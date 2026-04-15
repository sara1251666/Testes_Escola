package controller;

import bll.DocenteBLL;
import common.ConsoleUI;
import common.OperacaoCanceladaException;
import common.OperationResult;
import model.RepositorioSessao;
import view.DocenteView;

/**
 * Controller responsável pelas operações do Docente.
 * Implementa o padrão de cancelamento via exceção e delega I/O para ConsoleUI.
 */
public class DocenteController {
    private final DocenteView view;
    private final DocenteBLL bll;
    private final int idDocenteLogado;

    public DocenteController() {
        this.view = new DocenteView();
        this.bll = new DocenteBLL();
        // Obtém o ID da sessão global centralizada
        this.idDocenteLogado = RepositorioSessao.getInstance().getIdUtilizadorLogado();
    }

    public void iniciar() {
        boolean correr = true;
        while (correr) {
            try {
                int opcao = view.mostrarMenuPrincipal();
                switch (opcao) {
                    case 1:
                        fluxoConsultarAlunos();
                        break;
                    case 2:
                        fluxoLancarNotas();
                        break;
                    case 3:
                        fluxoAlterarPassword();
                        break;
                    case 0:
                        RepositorioSessao.getInstance().limparSessao();
                        correr = false;
                        break;
                    default:
                        view.mostrarMensagem("Opção inválida.", false);
                }
            } catch (OperacaoCanceladaException e) {
                // Captura o cancelamento e limpa o fluxo visual
                view.mostrarMensagem("Operação cancelada pelo utilizador.", false);
            }
        }
    }

    private void fluxoConsultarAlunos() {
        OperationResult<String> resultado = bll.obterRelatorioMeusAlunos(idDocenteLogado);
        if (resultado.isSuccess()) {
            view.mostrarEstatisticas(resultado.getData());
        } else {
            view.mostrarMensagem(resultado.getMessage(), false);
        }
    }

    private void fluxoLancarNotas() throws OperacaoCanceladaException {
        ConsoleUI.imprimirCabecalho("Lançamento de Notas");

        int numAluno = ConsoleUI.lerInteiro("Número do Aluno");
        String siglaUc = ConsoleUI.lerString("Sigla da UC");

        double nNormal = ConsoleUI.lerDouble("Nota Época Normal (-1 para falta)");
        double nRecurso = ConsoleUI.lerDouble("Nota Época Recurso (-1 para falta)");
        double nEspecial = ConsoleUI.lerDouble("Nota Época Especial (-1 para falta)");

        int ano = RepositorioSessao.getInstance().getAnoAtual();

        OperationResult<Void> res = bll.lancarNotas(idDocenteLogado, numAluno, siglaUc, ano, nNormal, nRecurso, nEspecial);
        view.mostrarMensagem(res.getMessage(), res.isSuccess());
    }

    private void fluxoAlterarPassword() throws OperacaoCanceladaException {
        ConsoleUI.imprimirCabecalho("Alterar Password");
        String novaPass = ConsoleUI.lerPassword("Nova Password");

        OperationResult<Void> res = bll.alterarPassword(idDocenteLogado, novaPass);
        view.mostrarMensagem(res.getMessage(), res.isSuccess());
    }
}