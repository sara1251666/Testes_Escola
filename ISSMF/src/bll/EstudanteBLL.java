package bll;

import common.EmailService;
import common.OperationResult;
import common.PasswordGenerator;
import common.SecurityUtil;
import common.ValidacaoUtil;
import dal.*;
import model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * BLL do Estudante.
 *
 * Regras de negócio implementadas aqui:
 *  - Auto-matrícula: gera numMec, email, password e credenciais.
 *  - Propinas: pagamento parcial/total, saldo atualizado no CSV.
 *  - Transição de ano: exige saldo=0 e aproveitamento >= 60%.
 *  - Consulta de notas e histórico de pagamentos.
 */
public class EstudanteBLL {

    private final EstudanteDAL  estudanteDAL  = new EstudanteDAL();
    private final AutenticacaoDAL authDAL     = new AutenticacaoDAL();
    private final CursoDAL      cursoDAL      = new CursoDAL();
    private final PagamentoDAL  pagamentoDAL  = new PagamentoDAL();
    private final AvaliacaoDAL  avaliacaoDAL  = new AvaliacaoDAL();
    private final UcDAL         ucDAL         = new UcDAL();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // =========================================================================
    // AUTO-MATRÍCULA / REGISTO PELA SECRETARIA
    // =========================================================================

    /**
     * Regista um novo estudante no sistema.
     *
     * Validações (por ordem):
     *  1. Formato da data de nascimento válido.
     *  2. Idade mínima de 17 anos — impede inscrição de menores de idade.
     *  3. NIF duplicado → erro.
     *  4. Curso existe → erro se não (não exige que esteja ativo — estudantes
     *     podem inscrever-se antes de o quórum ser atingido).
     *  5. Gera numMec (ano×10000+seq), email institucional, password aleatória.
     *  6. Grava estudante + credenciais.
     *  7. Auto-ativa o curso ao atingir ≥5 inscritos (quórum do 1.º ano).
     *  8. Envia email (assíncrono, sem mostrar password na consola).
     */
    public OperationResult<Estudante> registar(String nome, String nif, String emailPessoal,
                                                String dataNascimento, String morada,
                                                String siglaCurso, int anoAtual) {
        // 1. Formato da data
        if (!ValidacaoUtil.formatoDataValido(dataNascimento)) {
            return OperationResult.error("Data de nascimento inválida. Use o formato dd-MM-yyyy.");
        }

        // 2. Idade mínima (17 anos)
        if (!ValidacaoUtil.idadeSuficiente(dataNascimento, ValidacaoUtil.IDADE_MINIMA_ESTUDANTE)) {
            int idade;
            try { idade = ValidacaoUtil.calcularIdade(dataNascimento); }
            catch (Exception e) { idade = 0; }
            return OperationResult.error(String.format(
                    "Inscrição recusada. O candidato tem %d ano(s); a idade mínima é %d anos.",
                    idade, ValidacaoUtil.IDADE_MINIMA_ESTUDANTE));
        }

        // 3. NIF duplicado?
        if (estudanteDAL.existeNif(nif)) {
            return OperationResult.error("Já existe um estudante com o NIF " + nif + ".");
        }

        // 4. Curso existe? (não verifica estado ativo — permite inscrição antes do quórum)
        Curso curso = cursoDAL.obterPorSigla(siglaCurso);
        if (curso == null) {
            return OperationResult.error("Curso '" + siglaCurso + "' não encontrado.");
        }

        // 5. Gerar numMec e email institucional (ano×10000+seq → ex: 20260001)
        int    numMec     = estudanteDAL.gerarProximoNumMec(anoAtual);
        String emailInst  = numMec + "@issmf.ipp.pt";
        String emailFinal = (emailPessoal != null && !emailPessoal.isBlank()
                && !emailPessoal.equalsIgnoreCase("nao"))
                ? emailPessoal.trim() : emailInst;

        if (!emailFinal.equals(emailInst) && estudanteDAL.existeEmail(emailFinal)) {
            return OperationResult.error("Já existe um estudante com o email " + emailFinal + ".");
        }

        // 6. Criar entidade e persistir
        Estudante estudante = new Estudante(numMec, emailFinal, nome, nif, morada,
                dataNascimento, anoAtual, siglaCurso);
        estudante.setSaldoDevedor(curso.getPropina());

        if (!estudanteDAL.adicionar(estudante)) {
            return OperationResult.error("Erro ao gravar os dados do estudante.");
        }

        // Credenciais
        String passPlain = PasswordGenerator.gerarPassword(8);
        String passHash  = SecurityUtil.hashPassword(passPlain);
        if (!authDAL.criarCredencial(emailFinal, passHash, "ESTUDANTE", String.valueOf(numMec))) {
            return OperationResult.error("Erro ao criar credenciais de acesso.");
        }

        // 7. Auto-activação do curso: se ≥ 5 inscritos → "Ativo"
        int totalInscritos = estudanteDAL.contarPorCurso(siglaCurso);
        boolean cursoAcabouDeAtivado = false;
        if (totalInscritos >= 5 && !curso.estaAtivo()) {
            curso.setEstado("Ativo");
            cursoDAL.atualizar(curso);
            cursoAcabouDeAtivado = true;
        }

        // 8. Email assíncrono — password NUNCA aparece na consola
        EmailService.enviarCredenciais(emailFinal, nome, String.valueOf(numMec), passPlain);

        String mensagem = "Estudante registado! Nº " + numMec + " | Email: " + emailFinal
                + (cursoAcabouDeAtivado ? " | Curso '" + siglaCurso + "' ativado (quórum atingido)." : ".");
        return OperationResult.success(mensagem, estudante);
    }

    // =========================================================================
    // PROPINAS
    // =========================================================================

    /**
     * Pagar propina — total ou parcial.
     *
     * Regra: o valor pago não pode exceder o saldo devedor atual.
     */
    public OperationResult<Estudante> pagarPropina(Estudante estudante, double valorPago, int anoAtual) {
        if (valorPago <= 0) {
            return OperationResult.error("O valor a pagar tem de ser superior a zero.");
        }
        if (valorPago > estudante.getSaldoDevedor()) {
            return OperationResult.error(String.format(
                    "O valor (%.2f€) excede o saldo devedor atual (%.2f€).",
                    valorPago, estudante.getSaldoDevedor()));
        }

        // Atualizar saldo
        double novoSaldo = estudante.getSaldoDevedor() - valorPago;
        estudante.setSaldoDevedor(Math.round(novoSaldo * 100.0) / 100.0); // evitar float drift

        if (!estudanteDAL.atualizar(estudante)) {
            return OperationResult.error("Erro ao atualizar o saldo no ficheiro.");
        }

        // Registar no histórico de pagamentos
        String hoje = LocalDate.now().format(FMT);
        pagamentoDAL.registar(new Pagamento(estudante.getNumMec(), anoAtual, valorPago, hoje));

        String msg = estudante.getSaldoDevedor() == 0
                ? String.format("Pagamento de %.2f€ registado. Propina liquidada!", valorPago)
                : String.format("Pagamento de %.2f€ registado. Saldo restante: %.2f€.",
                        valorPago, estudante.getSaldoDevedor());

        return OperationResult.success(msg, estudante);
    }

    /**
     * Devolve o histórico de pagamentos de um estudante no ano corrente.
     */
    public OperationResult<List<Pagamento>> obterHistoricoPagamentos(int numMec, int anoAtual) {
        List<Pagamento> historico = pagamentoDAL.listarPorAlunoEAno(numMec, anoAtual);
        if (historico.isEmpty()) {
            return OperationResult.error("Sem registos de pagamento para este ano letivo.");
        }
        return OperationResult.success("Histórico carregado.", historico);
    }

    // =========================================================================
    // TRANSIÇÃO DE ANO LETIVO
    // =========================================================================

    /**
     * Verifica e processa a transição de ano de um estudante.
     *
     * REGRAS (Enunciado v1.1):
     *  1. Propina do ano corrente totalmente paga (saldo == 0).
     *  2. Aproveitamento >= 60% nas UCs do ano corrente.
     *     (ex: 1.º ano com 5 UCs → precisa de 3 aprovações; 2.º ano com 5 → 3)
     *  3. Não pode transitar se já estiver no último ano (3.º) sem aprovação completa.
     */
    public OperationResult<Estudante> transitarAno(Estudante estudante, int anoAtual) {
        // Regra 1: propina paga
        if (estudante.getSaldoDevedor() > 0) {
            return OperationResult.error(String.format(
                    "Transição negada. Existe saldo de propina em dívida: %.2f€.",
                    estudante.getSaldoDevedor()));
        }

        int anoAtualCurricular = estudante.getAnoCurricular();

        if (anoAtualCurricular >= 3) {
            return OperationResult.error("O estudante já se encontra no 3.º ano ou concluiu o curso.");
        }

        // Regra 2: aproveitamento >= 60%
        List<String> ucsDoAno = ucDAL.obterSiglasPorCursoEAno(estudante.getSiglaCurso(), anoAtualCurricular);
        if (ucsDoAno.isEmpty()) {
            return OperationResult.error("Não foram encontradas UCs para o ano " + anoAtualCurricular + ".");
        }

        List<Avaliacao> avaliacoes = avaliacaoDAL.listarPorSiglasUC(ucsDoAno);

        int totalUCs   = ucsDoAno.size();
        int aprovados  = 0;

        for (String siglaUC : ucsDoAno) {
            boolean ucAprovada = avaliacoes.stream()
                    .filter(av -> av.getSiglaUC().equalsIgnoreCase(siglaUC)
                            && av.getNumMec() == estudante.getNumMec()
                            && av.getAnoLetivo() == anoAtual)
                    .anyMatch(Avaliacao::estaAprovado);
            if (ucAprovada) aprovados++;
        }

        double percentagem = totalUCs > 0 ? (double) aprovados / totalUCs : 0;

        if (percentagem < 0.60) {
            return OperationResult.error(String.format(
                    "Aproveitamento insuficiente: %d/%d UCs aprovadas (%.0f%%). Mínimo exigido: 60%%.",
                    aprovados, totalUCs, percentagem * 100));
        }

        // Tudo ok — avançar o ano curricular e aplicar nova propina
        estudante.setAnoCurricular(anoAtualCurricular + 1);
        Curso curso = cursoDAL.obterPorSigla(estudante.getSiglaCurso());
        if (curso != null) {
            estudante.setSaldoDevedor(curso.getPropina());
        }

        if (!estudanteDAL.atualizar(estudante)) {
            return OperationResult.error("Erro ao atualizar os dados do estudante.");
        }

        return OperationResult.success(String.format(
                "Transição aprovada! O estudante avançou para o %d.º ano. Nova propina: %.2f€.",
                estudante.getAnoCurricular(), estudante.getSaldoDevedor()), estudante);
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    public Estudante obterPorId(int numMec) {
        return estudanteDAL.obterPorId(numMec);
    }

    /**
     * Devolve as avaliações de um estudante no ano letivo atual.
     */
    public OperationResult<List<Avaliacao>> obterNotas(int numMec, int anoAtual) {
        List<Avaliacao> notas = avaliacaoDAL.listarPorAluno(numMec, anoAtual);
        if (notas.isEmpty()) {
            return OperationResult.error("Ainda não existem notas lançadas para este ano letivo.");
        }
        return OperationResult.success("Notas carregadas.", notas);
    }

    public OperationResult<Estudante> atualizarMorada(Estudante e, String novaMorada) {
        e.setMorada(novaMorada);
        return estudanteDAL.atualizar(e)
                ? OperationResult.success("Morada atualizada.", e)
                : OperationResult.error("Erro ao atualizar a morada.");
    }

    public OperationResult<Void> alterarPassword(String emailEstudante, String novaPass) {
        if (novaPass == null || novaPass.length() < 6) {
            return OperationResult.error("A password deve ter pelo menos 6 caracteres.");
        }
        String hash = SecurityUtil.hashPassword(novaPass);
        return authDAL.atualizarPassword(emailEstudante, hash)
                ? OperationResult.success("Password atualizada com segurança.")
                : OperationResult.error("Erro ao atualizar a password.");
    }
}
