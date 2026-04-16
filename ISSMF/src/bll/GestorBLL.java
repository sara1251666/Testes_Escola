package bll;

import common.EmailService;
import common.OperationResult;
import common.PasswordGenerator;
import common.SecurityUtil;
import common.ValidacaoUtil;
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

    private final EstudanteDAL    estudanteDAL    = new EstudanteDAL();
    private final DocenteDAL      docenteDAL      = new DocenteDAL();
    private final CursoDAL        cursoDAL        = new CursoDAL();
    private final UcDAL           ucDAL           = new UcDAL();
    private final AutenticacaoDAL authDAL         = new AutenticacaoDAL();
    private final DepartamentoDAL departamentoDAL = new DepartamentoDAL();

    /**
     * Regista um novo docente.
     *
     * Validações:
     *  1. Formato da data de nascimento.
     *  2. Idade mínima de 21 anos.
     *  3. NIF duplicado.
     *  4. Geração de sigla única (iniciais do nome, sem repetição).
     *  5. Criação de credenciais e envio de email.
     */
    public OperationResult<Docente> registarDocente(String nome, String nif,
                                                     String morada, String dataNascimento) {
        // 1. Formato da data
        if (!ValidacaoUtil.formatoDataValido(dataNascimento)) {
            return OperationResult.error("Data de nascimento inválida. Use o formato dd-MM-yyyy.");
        }

        // 2. Idade mínima (21 anos)
        if (!ValidacaoUtil.idadeSuficiente(dataNascimento, ValidacaoUtil.IDADE_MINIMA_DOCENTE)) {
            int idade;
            try { idade = ValidacaoUtil.calcularIdade(dataNascimento); }
            catch (Exception e) { idade = 0; }
            return OperationResult.error(String.format(
                    "Registo recusado. O candidato tem %d ano(s); a idade mínima para docente é %d anos.",
                    idade, ValidacaoUtil.IDADE_MINIMA_DOCENTE));
        }

        // 3. NIF duplicado?
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

        if (!authDAL.criarCredencial(email, passHash, "DOCENTE", sigla)) {
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

        // Novo curso começa "Inativo" — torna-se "Ativo" automaticamente
        // quando atingir ≥5 inscritos (quórum do 1.º ano letivo).
        Curso curso = new Curso(sigla, nome, siglaDep, propina, "Inativo");

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
     *  1. A UC não pode já existir NESTE curso (mas pode existir noutros — requisito v1.0).
     *  2. Curso existe.
     *  3. Docente existe.
     *  4. Ano curricular: 1, 2 ou 3.
     *  5. Máximo 5 UCs por ano por curso.
     */
    public OperationResult<Void> criarUC(String sigla, String nome, String siglaCurso,
                                          int anoCurricular, String siglaDocente) {
        // Verifica duplicado NESTE curso (não globalmente — uma UC pode estar em vários cursos)
        if (ucDAL.existeNoCurso(sigla, siglaCurso)) {
            return OperationResult.error(
                    "A UC '" + sigla + "' já existe no curso '" + siglaCurso + "'.");
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

    /**
     * Regista um novo departamento.
     */
    public OperationResult<Void> registarDepartamento(String sigla, String nome) {
        if (sigla == null || sigla.isBlank() || nome == null || nome.isBlank()) {
            return OperationResult.error("A sigla e o nome do departamento são obrigatórios.");
        }
        if (departamentoDAL.existe(sigla)) {
            return OperationResult.error("Já existe um departamento com a sigla '" + sigla + "'.");
        }
        return departamentoDAL.adicionar(sigla, nome)
                ? OperationResult.success("Departamento '" + nome + "' (" + sigla + ") registado com sucesso.")
                : OperationResult.error("Erro ao gravar o departamento.");
    }

    /**
     * Lista todos os departamentos.
     */
    public OperationResult<String> listarDepartamentos() {
        List<String> lista = departamentoDAL.listarTodos();
        if (lista.isEmpty()) {
            return OperationResult.success("Nenhum departamento registado.", "");
        }
        StringBuilder sb = new StringBuilder();
        for (String dep : lista) {
            sb.append("  ").append(dep).append("\n");
        }
        return OperationResult.success("Departamentos carregados.", sb.toString());
    }

    // =========================================================================
    // GESTÃO DE ESTADO (ATIVAR / DESATIVAR)
    // =========================================================================

    /**
     * Ativa ou desativa um estudante.
     * Um estudante inativo não consegue fazer login.
     */
    public OperationResult<Void> alterarEstadoEstudante(int numMec, boolean ativar) {
        Estudante e = estudanteDAL.obterPorId(numMec);
        if (e == null) {
            return OperationResult.error("Não foi encontrado nenhum estudante com o nº " + numMec + ".");
        }
        if (e.isAtivo() == ativar) {
            return OperationResult.error("O estudante já se encontra " + (ativar ? "ativo" : "inativo") + ".");
        }
        e.setAtivo(ativar);
        return estudanteDAL.atualizar(e)
                ? OperationResult.success("Estudante " + numMec + " " + (ativar ? "ativado" : "desativado") + " com sucesso.")
                : OperationResult.error("Erro ao atualizar o estado do estudante.");
    }

    /**
     * Ativa ou desativa um docente.
     * Um docente inativo não consegue fazer login.
     */
    public OperationResult<Void> alterarEstadoDocente(String sigla, boolean ativar) {
        Docente d = docenteDAL.obterPorSigla(sigla);
        if (d == null) {
            return OperationResult.error("Não foi encontrado nenhum docente com a sigla '" + sigla + "'.");
        }
        if (d.isAtivo() == ativar) {
            return OperationResult.error("O docente já se encontra " + (ativar ? "ativo" : "inativo") + ".");
        }
        d.setAtivo(ativar);
        return docenteDAL.atualizar(d)
                ? OperationResult.success("Docente " + sigla + " " + (ativar ? "ativado" : "desativado") + " com sucesso.")
                : OperationResult.error("Erro ao atualizar o estado do docente.");
    }

    /**
     * Lista todos os estudantes com estado (ativo/inativo).
     */
    public OperationResult<String> listarEstudantesComEstado() {
        java.util.List<Estudante> todos = estudanteDAL.listarTodos();
        if (todos.isEmpty()) return OperationResult.error("Não existem estudantes registados.");

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  %-10s %-25s %-8s %s%n", "Nº Mec.", "Nome", "Curso", "Estado"));
        sb.append("  ").append("-".repeat(60)).append("\n");
        for (Estudante e : todos) {
            sb.append(String.format("  %-10d %-25s %-8s %s%n",
                    e.getNumMec(), e.getNome(), e.getSiglaCurso(),
                    e.isAtivo() ? "[ATIVO]" : "[INATIVO]"));
        }
        return OperationResult.success("Lista gerada.", sb.toString());
    }

    /**
     * Lista todos os docentes com estado (ativo/inativo).
     */
    public OperationResult<String> listarDocentesComEstado() {
        java.util.List<Docente> todos = docenteDAL.listarTodos();
        if (todos.isEmpty()) return OperationResult.error("Não existem docentes registados.");

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  %-6s %-25s %-30s %s%n", "Sigla", "Nome", "Email", "Estado"));
        sb.append("  ").append("-".repeat(70)).append("\n");
        for (Docente d : todos) {
            sb.append(String.format("  %-6s %-25s %-30s %s%n",
                    d.getSigla(), d.getNome(), d.getEmail(),
                    d.isAtivo() ? "[ATIVO]" : "[INATIVO]"));
        }
        return OperationResult.success("Lista gerada.", sb.toString());
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
