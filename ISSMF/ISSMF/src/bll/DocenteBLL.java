package bll;

import common.OperationResult;
import dal.AvaliacaoDAL;
import dal.EstudanteDAL;
import dal.UcDAL;
import dal.AutenticacaoDAL;
import common.SecurityUtil;

public class DocenteBLL {
    private final AvaliacaoDAL avaliacaoDAL = new AvaliacaoDAL();
    private final EstudanteDAL estudanteDAL = new EstudanteDAL();
    private final UcDAL ucDAL = new UcDAL();
    private final AutenticacaoDAL authDAL = new AutenticacaoDAL();

    public OperationResult<Void> lancarNotas(int idDocente, int numAluno, String siglaUc, int anoLetivo, double nNormal, double nRecurso, double nEspecial) {

        if (!ucDAL.docenteLecionaUC(idDocente, siglaUc)) {
            return OperationResult.error("Acesso negado: Não é o docente responsável pela UC " + siglaUc.toUpperCase());
        }

        if (!estudanteDAL.existeEstudante(numAluno)) {
            return OperationResult.error("Não foi encontrado nenhum estudante com o número " + numAluno);
        }

       boolean sucesso = avaliacaoDAL.registarAvaliacao(numAluno, siglaUc, anoLetivo, nNormal, nRecurso, nEspecial);

        if (sucesso) {
            return OperationResult.success("Notas lançadas com sucesso para o aluno " + numAluno + ".", null);
        }
        return OperationResult.error("Ocorreu um erro ao gravar no ficheiro de avaliações.");
    }

    public OperationResult<String> obterRelatorioMeusAlunos(int idDocente) {
       String relatorio = avaliacaoDAL.gerarRelatorioAlunosPorDocente(idDocente);

        if (relatorio == null || relatorio.isEmpty()) {
            return OperationResult.error("Não tem alunos inscritos ou não há dados de avaliação.");
        }

        return OperationResult.success("Relatório gerado.", relatorio);
    }

    public OperationResult<Void> alterarPassword(int idDocente, String novaPasswordPlain) {
        if (novaPasswordPlain.length() < 6) {
            return OperationResult.error("A password deve ter pelo menos 6 caracteres.");
        }

        String novaPasswordHash = SecurityUtil.hashPassword(novaPasswordPlain);

        boolean atualizado = authDAL.atualizarPasswordPorId(idDocente, novaPasswordHash);

        if (atualizado) return OperationResult.success("Password atualizada com segurança.", null);
        return OperationResult.error("Falha ao atualizar a password no ficheiro.");
    }
}