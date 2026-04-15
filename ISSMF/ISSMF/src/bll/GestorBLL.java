package bll;

import common.OperationResult;
import dal.AutenticacaoDAL;
import dal.CursoDAL;
import dal.EstudanteDAL;
import dal.UcDAL;
import common.SecurityUtil;

public class GestorBLL {
    private final CursoDAL cursoDAL = new CursoDAL();
    private final EstudanteDAL estudanteDAL = new EstudanteDAL();
    private final UcDAL ucDAL = new UcDAL();
    private final AutenticacaoDAL authDAL = new AutenticacaoDAL();

    public OperationResult<Void> criarCurso(String sigla, String nome, String departamento) { /* ... */ return null; }
    public OperationResult<Boolean> verificarSePodeEditarCurso(String sigla) { /* ... */ return null; }
    public OperationResult<Void> editarCurso(String sigla, String novoNome, String novoDep) { /* ... */ return null; }
    public OperationResult<String> obterRelatorioDividas() { /* ... */ return null; }


    public OperationResult<String> obterEstatisticasGlobais() {
        int totalAlunos = estudanteDAL.contarTotalEstudantes();
        int totalCursos = cursoDAL.contarTotalCursos();

        String report = ">> Total de Cursos Ativos: " + totalCursos + "\n" +
                ">> Total de Estudantes Matriculados: " + totalAlunos;

        return OperationResult.success("Estatísticas carregadas.", report);
    }

    public OperationResult<Void> avancarAnoLetivo() {
        return OperationResult.error("Funcionalidade de Transição de Ano em construção.");
    }

    public OperationResult<Void> alterarPassword(int idGestor, String novaPasswordPlain) {
        if (novaPasswordPlain.length() < 6) {
            return OperationResult.error("A password deve ter pelo menos 6 caracteres.");
        }
        String novaPasswordHash = SecurityUtil.hashPassword(novaPasswordPlain);
        boolean atualizado = authDAL.atualizarPasswordPorId(idGestor, novaPasswordHash);

        if (atualizado) return OperationResult.success("Password atualizada com segurança.", null);
        return OperationResult.error("Falha ao atualizar a password no ficheiro.");
    }

    /**
     * Cria uma nova Unidade Curricular no sistema.
     * Aplica validações de integridade antes de persistir na DAL.
     */
    public OperationResult<Void> criarUc(String sigla, String nome, String siglaCurso, int ano, int idDocente) {
        if (ucDAL.existeUc(sigla)) {
            return OperationResult.error("Já existe uma UC registada com a sigla " + sigla.toUpperCase());
        }

        if (!cursoDAL.existeCurso(siglaCurso)) {
            return OperationResult.error("O curso '" + siglaCurso.toUpperCase() + "' não existe.");
        }

        if (new dal.DocenteDAL().obterDocentePorId(idDocente) == null) {
            return OperationResult.error("O docente com ID " + idDocente + " não foi encontrado.");
        }

        boolean gravado = ucDAL.adicionarUc(sigla.toUpperCase(), nome, siglaCurso.toUpperCase(), ano, idDocente);

        if (gravado) {
            return OperationResult.success("Unidade Curricular '" + nome + "' criada com sucesso.", null);
        }
        return OperationResult.error("Erro técnico ao gravar a UC no ficheiro.");
    }
}