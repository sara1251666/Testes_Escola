package controller;

import bll.DocenteBLL;
import common.ConsoleUI;
import common.OperacaoCanceladaException;
import common.OperationResult;
import model.Docente;
import model.RepositorioSessao;
import view.DocenteView;

/**
 * Controller do Portal do Docente.
 *
 * O docente é identificado pela sua SIGLA guardada na sessão
 * via RepositorioSessao.getSiglaDocenteLogado().
 */
public class DocenteController {
    private final DocenteView view;
    private final DocenteBLL  bll;
    private final String      siglaDocente;

    public DocenteController() {
        this.view          = new DocenteView();
        this.bll           = new DocenteBLL();
        this.siglaDocente  = RepositorioSessao.getInstance().getSiglaDocenteLogado();
    }

    public void iniciar() {
        boolean correr = true;
        while (correr) {
            try {
                int opcao = view.mostrarMenuPrincipal();
                switch (opcao) {
                    case 1 -> fluxoVerFicha();
                    case 2 -> fluxoConsultarAlunos();
                    case 3 -> fluxoLancarNotas();
                    case 4 -> fluxoAlterarPassword();
                    case 0 -> {
                        RepositorioSessao.getInstance().limparSessao();
                        correr = false;
                    }
                    default -> view.mostrarMensagem("Opção inválida.", false);
                }
            } catch (OperacaoCanceladaException e) {
                view.mostrarMensagem("Operação cancelada.", false);
            }
        }
    }

    // =========================================================================
    // FLUXOS
    // =========================================================================

    private void fluxoVerFicha() {
        OperationResult<Docente> res = bll.obterFicha(siglaDocente);
        if (res.isSuccess()) {
            view.mostrarFicha(res.getData());
        } else {
            view.mostrarMensagem(res.getMessage(), false);
        }
    }

    private void fluxoConsultarAlunos() {
        OperationResult<String> res = bll.obterRelatorioMeusAlunos(siglaDocente);
        if (res.isSuccess()) {
            view.mostrarEstatisticas(res.getData());
        } else {
            view.mostrarMensagem(res.getMessage(), false);
        }
    }

    private void fluxoLancarNotas() throws OperacaoCanceladaException {
        ConsoleUI.imprimirCabecalho("Lançamento de Notas");
        ConsoleUI.imprimirDicaFormulario();

        int    numAluno  = ConsoleUI.lerInteiro("Número do Aluno");
        String siglaUc   = ConsoleUI.lerString("Sigla da UC").toUpperCase();
        double nNormal   = ConsoleUI.lerNota("Nota Época Normal  (-1 para falta)");
        double nRecurso  = ConsoleUI.lerNota("Nota Época Recurso (-1 para falta)");
        double nEspecial = ConsoleUI.lerNota("Nota Época Especial(-1 para falta)");

        int anoLetivo = RepositorioSessao.getInstance().getAnoAtual();

        OperationResult<Void> res = bll.lancarNotas(
                siglaDocente, numAluno, siglaUc, anoLetivo, nNormal, nRecurso, nEspecial);
        view.mostrarMensagem(res.getMessage(), res.isSuccess());
    }

    private void fluxoAlterarPassword() throws OperacaoCanceladaException {
        ConsoleUI.imprimirCabecalho("Alterar Password");
        ConsoleUI.imprimirDicaFormulario();
        String novaPass = ConsoleUI.lerPassword("Nova Password (mín. 6 caracteres)");
        String confirmar= ConsoleUI.lerPassword("Confirmar nova Password");
        if (!novaPass.equals(confirmar)) {
            view.mostrarMensagem("As passwords não coincidem.", false);
            return;
        }
        OperationResult<Void> res = bll.alterarPassword(siglaDocente, novaPass);
        view.mostrarMensagem(res.getMessage(), res.isSuccess());
    }
}
