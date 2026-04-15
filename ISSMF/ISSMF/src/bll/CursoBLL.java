package bll;

import common.OperationResult;
import dal.CursoDAL;
import dal.EstudanteDAL;
import dal.UcDAL;

public class CursoBLL {
    private final CursoDAL cursoDAL = new CursoDAL();
    private final EstudanteDAL estudanteDAL = new EstudanteDAL();
    private final UcDAL ucDAL = new UcDAL();

    public OperationResult<Boolean> verificarCondicoesArranque(String siglaCurso, int anoLetivoCurso) {
        if (!cursoDAL.existeCurso(siglaCurso)) {
            return OperationResult.error("Curso não encontrado.");
        }

        int inscritos = estudanteDAL.contarAlunosNoCurso(siglaCurso); // Método assumido na DAL

        if (anoLetivoCurso == 1 && inscritos < 5) {
            return OperationResult.error("O 1º ano deste curso não pode arrancar. Mínimo de 5 alunos inscritos não atingido (Atuais: " + inscritos + ").");
        } else if (anoLetivoCurso > 1 && inscritos < 1) {
            return OperationResult.error("O " + anoLetivoCurso + "º ano não pode arrancar. Nenhum aluno inscrito.");
        }

        return OperationResult.success("Condições de arranque reunidas.", true);
    }
}