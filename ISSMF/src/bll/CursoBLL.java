package bll;

import common.OperationResult;
import dal.CursoDAL;
import dal.EstudanteDAL;

/**
 * BLL auxiliar para lógica de Cursos.
 * A verificação de quórum principal está em GestorBLL.verificarQuorum().
 */
public class CursoBLL {
    private final CursoDAL      cursoDAL     = new CursoDAL();
    private final EstudanteDAL  estudanteDAL = new EstudanteDAL();

    public OperationResult<Boolean> verificarCondicoesArranque(String siglaCurso, int anoLetivoCurso) {
        if (!cursoDAL.existe(siglaCurso)) {
            return OperationResult.error("Curso não encontrado.");
        }

        int inscritos = estudanteDAL.contarPorCurso(siglaCurso);

        if (anoLetivoCurso == 1 && inscritos < 5) {
            return OperationResult.error(
                    "O 1.º ano deste curso não pode arrancar. "
                    + "Mínimo 5 alunos inscritos (atuais: " + inscritos + ").");
        } else if (anoLetivoCurso > 1 && inscritos < 1) {
            return OperationResult.error(
                    "O " + anoLetivoCurso + ".º ano não pode arrancar. Nenhum aluno inscrito.");
        }

        return OperationResult.success("Condições de arranque reunidas.", true);
    }
}
