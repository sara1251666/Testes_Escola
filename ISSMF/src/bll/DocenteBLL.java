package bll;

import common.OperationResult;
import common.SecurityUtil;
import dal.AvaliacaoDAL;
import dal.AutenticacaoDAL;
import dal.DocenteDAL;
import dal.EstudanteDAL;
import dal.UcDAL;
import model.Avaliacao;
import model.Docente;

import java.util.List;

/**
 * BLL do Docente.
 *
 * O docente é identificado pela sua SIGLA (String de 3 letras),
 * não por um id numérico.
 */
public class DocenteBLL {
    private final AvaliacaoDAL  avaliacaoDAL = new AvaliacaoDAL();
    private final EstudanteDAL  estudanteDAL = new EstudanteDAL();
    private final UcDAL         ucDAL        = new UcDAL();
    private final AutenticacaoDAL authDAL    = new AutenticacaoDAL();
    private final DocenteDAL    docenteDAL   = new DocenteDAL();

    /**
     * Lança notas para um aluno numa UC.
     * Valida que o docente (por sigla) é responsável pela UC.
     *
     * @param siglaDocente Sigla do docente logado (ex: "ABC").
     */
    public OperationResult<Void> lancarNotas(String siglaDocente, int numAluno,
                                              String siglaUc, int anoLetivo,
                                              double nNormal, double nRecurso, double nEspecial) {
        // 1. Verificar que o docente é responsável pela UC
        if (!ucDAL.docenteLecionaUC(siglaDocente, siglaUc)) {
            return OperationResult.error(
                    "Acesso negado: não é o docente responsável pela UC '" + siglaUc.toUpperCase() + "'.");
        }

        // 2. Verificar que o aluno existe
        if (!estudanteDAL.existePorId(numAluno)) {
            return OperationResult.error(
                    "Não foi encontrado nenhum estudante com o número " + numAluno + ".");
        }

        // 3. Registar avaliação (cria ou atualiza)
        boolean sucesso = avaliacaoDAL.registarAvaliacao(
                numAluno, siglaUc, anoLetivo, nNormal, nRecurso, nEspecial);

        return sucesso
                ? OperationResult.success("Notas lançadas com sucesso para o aluno " + numAluno + ".")
                : OperationResult.error("Ocorreu um erro ao gravar as avaliações.");
    }

    /**
     * Obtém o relatório de todos os alunos e notas das UCs do docente.
     *
     * @param siglaDocente Sigla do docente logado.
     */
    public OperationResult<String> obterRelatorioMeusAlunos(String siglaDocente) {
        List<String> minhasUcs = ucDAL.obterSiglasPorDocente(siglaDocente);
        if (minhasUcs.isEmpty()) {
            return OperationResult.error("Não tem unidades curriculares atribuídas.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  UCs atribuídas: %s%n%n", String.join(", ", minhasUcs)));

        String relatorio = avaliacaoDAL.gerarRelatorioAlunosPorDocente(siglaDocente);
        if (relatorio == null || relatorio.isBlank()) {
            sb.append("  Ainda não existem notas lançadas para as suas UCs.\n");
        } else {
            sb.append(relatorio);
        }

        return OperationResult.success("Relatório gerado.", sb.toString());
    }

    /**
     * Obtém a ficha pessoal do docente.
     */
    public OperationResult<Docente> obterFicha(String siglaDocente) {
        Docente d = docenteDAL.obterPorSigla(siglaDocente);
        if (d == null) return OperationResult.error("Dados do docente não encontrados.");
        return OperationResult.success("Ficha carregada.", d);
    }

    /**
     * Altera a password do docente identificado pela sua sigla.
     */
    public OperationResult<Void> alterarPassword(String siglaDocente, String novaPasswordPlain) {
        if (novaPasswordPlain == null || novaPasswordPlain.length() < 6) {
            return OperationResult.error("A password deve ter pelo menos 6 caracteres.");
        }
        String novaHash = SecurityUtil.hashPassword(novaPasswordPlain);
        boolean ok = authDAL.atualizarPasswordPorSigla(siglaDocente, novaHash);
        return ok
                ? OperationResult.success("Password atualizada com segurança.")
                : OperationResult.error("Erro ao atualizar a password.");
    }
}
