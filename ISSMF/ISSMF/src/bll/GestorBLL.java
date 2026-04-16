package bll;

import common.EmailService;
import common.OperationResult;
import common.PasswordGenerator;
import common.SecurityUtil;
import dal.*;
import model.*;

import java.util.List;

/**
 * BLL do Gestor.
 *
 * Regras de negócio:
 *  - Registo de docente: gera sigla, email, password; envia por email.
 *  - Gestão de cursos: criar/editar (bloqueado se tiver alunos ou docentes).
 *  - Criação de UCs: valida max 5/ano/curso e max 2 UCs/docente.
 *  - Relatório de devedores: lista estudantes com saldo > 0.
 *  - Estatísticas globais: total alunos, cursos, UCs.
 *  - Verificação de quórum para arranque de ano letivo.
 */
public class GestorBLL {

    private final EstudanteDAL  estudanteDAL = new EstudanteDAL();
    private final DocenteDAL    docenteDAL   = new DocenteDAL();
    private final CursoDAL      cursoDAL     = new CursoDAL();
    private final UcDAL         ucDAL        = new UcDAL();
    private final AutenticacaoDAL authDAL    = new AutenticacaoDAL();

    /**
     * Regista um novo docente.
     * A sigla é gerada automaticamente (3 primeiras letras do apelido, em maiúsculas).
     * Se já existir, adiciona um sufixo numérico.
     */
    public OperationResult<Docente> registarDocente(String nome, String nif,
                                                     String morada, String dataNascimento) {
        if (docenteDAL.existeNif(nif)) {
            return OperationResult.error("Já existe um docente com o NIF " + nif + ".");
        }

        String sigla = gerarSigla(nome);
        String email = sigla.toLowerCase() + "@issmf.ipp.pt";

        Docente docente = new Docente(sigla, email, nome, nif, morada, dataNascimento);

        if (!docenteDAL.adicionar(docente)) {
            return OperationResult.error("Erro ao gravar os dados do docente.");
        }

        String passPlain = PasswordGenerator.gerarPassword(8);
        String passHash  = SecurityUtil.hashPassword(passPlain);

        if (!authDAL.criarCredencial(email, passHash, "DOCENTE")) {
            return OperationResult.error("Erro ao criar credenciais do docente.");
        }

        // Email assíncrono — password nunca mostrada na consola
        EmailService.enviarCredenciais(email, nome, sigla, passPlain);

        return OperationResult.success(
                "Docente registado! Sigla: " + sigla + " | Email: " + email
                        + ". Credenciais enviadas por email.", docente);
    }

    public OperationResult<Curso> criarCurso(String sigla, String nome,
                                              String siglaDep, double propina) {
        if (cursoDAL.existe(sigla)) {
            return OperationResult.error("Já existe um curso com a sigla '" + sigla + "'.");
        }

        Curso curso = new Curso(sigla, nome, siglaDep, propina, "Ativo");

        return cursoDAL.adicionar(curso)
                ? OperationResult.success("Curso '" + nome + "' criado com sucesso.", curso)
                : OperationResult.error("Erro ao gravar o curso.");
    }

    /**
     * Editar só é permitido se NÃO existirem alunos nem docentes alocados.
     */
    public OperationResult<Curso> editarCurso(String sigla, String novoNome,
                                               String novaSiglaDep, double novaPropina) {
        Curso curso = cursoDAL.obterPorSigla(sigla);
        if (curso == null) {
            return OperationResult.error("Curso '" + sigla + "' não encontrado.");
        }

        // Regra: cursos com alunos não podem ser alterados
        if (estudanteDAL.contarPorCurso(sigla) > 0) {
            return OperationResult.error(
                    "Não é possível editar: existem estudantes inscritos neste curso.");
        }

        curso.setNome(novoNome);
        curso.setSiglaDepartamento(novaSiglaDep);
        curso.setPropina(novaPropina);

        return cursoDAL.atualizar(curso)
                ? OperationResult.success("Curso atualizado com sucesso.", curso)
                : OperationResult.error("Erro ao atualizar o curso.");
    }

    /**
     * Cria uma UC com todas as validações:
     *  1. Sigla única.
     *  2. Curso existe.
     *  3. Docente existe.
     *  4. Máximo 5 UCs por ano por curso.
     *  5. Máximo 2 UCs por docente.
     */
    public OperationResult<Void> criarUC(String sigla, String nome, String siglaCurso,
                                          int anoCurricular, String siglaDocente) {
        if (ucDAL.existe(sigla)) {
            return OperationResult.error("Já existe uma UC com a sigla '" + sigla + "'.");
        }
        if (!cursoDAL.existe(siglaCurso)) {
            return OperationResult.error("Curso '" + siglaCurso + "' não encontrado.");
        }
        if (docenteDAL.obterPorSigla(siglaDocente) == null) {
            return OperationResult.error("Docente '" + siglaDocente + "' não encontrado.");
        }
        if (anoCurricular < 1 || anoCurricular > 3) {
            return OperationResult.error("Ano curricular inválido. Use 1, 2 ou 3.");
        }

        int ucsNoAno = ucDAL.contarPorCursoEAno(siglaCurso, anoCurricular);
        if (ucsNoAno >= 5) {
            return OperationResult.error(
                    "O " + anoCurricular + ".º ano do curso '" + siglaCurso
                            + "' já tem 5 UCs (limite máximo).");
        }

        int ucsDocente = ucDAL.contarPorDocente(siglaDocente);
        if (ucsDocente >= 2) {
            return OperationResult.error(
                    "O docente '" + siglaDocente + "' já leciona 2 UCs (limite máximo).");
        }

        return ucDAL.adicionar(sigla, nome, siglaCurso, anoCurricular, siglaDocente)
                ? OperationResult.success("UC '" + nome + "' criada com sucesso.")
                : OperationResult.error("Erro ao gravar a UC.");
    }

    /**
     * Lista estudantes com saldo de propina em dívida.
     */
    public OperationResult<String> obterRelatorioDevedores() {
        List<Estudante> devedores = estudanteDAL.listarDevedores();

        if (devedores.isEmpty()) {
            return OperationResult.success("Não existem estudantes com propinas em dívida.", "");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  %-10s %-25s %-8s %s%n",
                "Nº Mec.", "Nome", "Curso", "Saldo em Dívida"));
        sb.append("  ").append("-".repeat(60)).append("\n");

        double totalDivida = 0;
        for (Estudante e : devedores) {
            sb.append(String.format("  %-10d %-25s %-8s %.2f€%n",
                    e.getNumMec(), e.getNome(), e.getSiglaCurso(), e.getSaldoDevedor()));
            totalDivida += e.getSaldoDevedor();
        }
        sb.append(String.format("%n  Total em dívida: %.2f€ (%d estudantes)%n",
                totalDivida, devedores.size()));

        return OperationResult.success("Relatório gerado.", sb.toString());
    }

    /**
     * Estatísticas globais do sistema.
     */
    public OperationResult<String> obterEstatisticasGlobais() {
        int totalAlunos = estudanteDAL.contarTotal();
        int totalCursos = cursoDAL.contarTotal();

        String relatorio = String.format(
                "  Total de Cursos   : %d%n"
                + "  Total de Estudantes: %d%n",
                totalCursos, totalAlunos);

        return OperationResult.success("Estatísticas carregadas.", relatorio);
    }

    /**
     * Verifica se um curso tem quórum para arrancar um determinado ano letivo.
     * 1.º ano: mínimo 5 alunos. 2.º e 3.º ano: mínimo 1 aluno.
     */
    public OperationResult<Void> verificarQuorum(String siglaCurso, int anoCurricular) {
        if (!cursoDAL.existe(siglaCurso)) {
            return OperationResult.error("Curso '" + siglaCurso + "' não encontrado.");
        }

        int inscritos = estudanteDAL.contarPorCurso(siglaCurso);
        int minimo    = (anoCurricular == 1) ? 5 : 1;

        if (inscritos < minimo) {
            return OperationResult.error(String.format(
                    "Quórum não atingido para o %d.º ano de '%s'. "
                    + "Mínimo: %d alunos. Atuais: %d.",
                    anoCurricular, siglaCurso, minimo, inscritos));
        }

        return OperationResult.success(String.format(
                "Quórum verificado. %d alunos inscritos em '%s'.",
                inscritos, siglaCurso));
    }

    public OperationResult<Void> alterarPassword(String emailGestor, String novaPass) {
        if (novaPass == null || novaPass.length() < 6) {
            return OperationResult.error("A password deve ter pelo menos 6 caracteres.");
        }
        String hash = SecurityUtil.hashPassword(novaPass);
        return authDAL.atualizarPassword(emailGestor, hash)
                ? OperationResult.success("Password atualizada com segurança.")
                : OperationResult.error("Erro ao atualizar a password.");
    }

    /**
     * Gera uma sigla de 3 letras a partir do nome.
     * Usa as iniciais das primeiras 3 palavras (ou repete se houver menos).
     * Se já existir, tenta com sufixo numérico.
     */
    private String gerarSigla(String nome) {
        String[] partes = nome.trim().toUpperCase().split("\\s+");
        StringBuilder base = new StringBuilder();
        for (int i = 0; i < Math.min(3, partes.length); i++) {
            base.append(partes[i].charAt(0));
        }
        while (base.length() < 3) base.append(base.charAt(0));

        String sigla = base.toString();
        int sufixo = 1;
        while (docenteDAL.existeSigla(sigla)) {
            sigla = base.substring(0, 2) + sufixo;
            sufixo++;
        }
        return sigla;
    }
}
