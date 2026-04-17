package controller;

import bll.AutenticacaoBLL;
import common.ConsoleUI;
import common.OperacaoCanceladaException;
import common.OperationResult;
import model.RepositorioSessao;
import view.MainView;

/**
 * Controller principal — pré-login.
 *
 * Recebe a MainView como dependência (injectada pelo construtor).
 * O loop de interacção está na MainView; este controller apenas
 * expõe acções públicas que a view invoca.
 *
 * Cadeia: Main → MainView.iniciar() → MainController (fazerLogin / iniciarAutoMatricula / recuperarPassword)
 */
public class MainController {

    private final AutenticacaoBLL authBll;
    private final MainView        view;

    public MainController(MainView view) {
        this.view    = view;
        this.authBll = new AutenticacaoBLL();
    }

    /** Fluxo de autenticação: pede credenciais e redireciona após sucesso. */
    public void fazerLogin() {
        boolean loginComSucesso = false;
        while (!loginComSucesso) {
            try {
                String[] creds = view.pedirCredenciais();
                OperationResult<String> res = authBll.fazerLogin(creds[0], creds[1]);

                if (res.isSuccess()) {
                    view.mostrarSucesso(res.getMessage());
                    loginComSucesso = true;
                    encaminharParaPainel(res.getData());
                } else {
                    view.mostrarErro(res.getMessage());
                }
            } catch (OperacaoCanceladaException e) {
                view.mostrarCancelamento();
                break;
            }
        }
    }

    /**
     * Atalho de arranque: avança o ano letivo do sistema sem entrar no painel gestor.
     * Requer autenticação de gestor para garantir que só o backoffice pode usar.
     */
    public void avancarAnoLetivo() {
        try {
            ConsoleUI.imprimirTitulo("Avançar Ano Letivo");
            ConsoleUI.imprimirDicaFormulario();
            ConsoleUI.imprimirInfo("Esta operação requer credenciais de Gestor.");

            String[] creds = view.pedirCredenciais();
            OperationResult<String> res = authBll.fazerLogin(creds[0], creds[1]);

            if (!res.isSuccess()) { view.mostrarErro(res.getMessage()); return; }
            if (!"GESTOR".equals(res.getData())) {
                view.mostrarErro("Acesso negado. Apenas o Gestor pode avançar o ano letivo.");
                RepositorioSessao.getInstance().limparSessao();
                return;
            }

            int anoAtual = RepositorioSessao.getInstance().getAnoAtual();
            ConsoleUI.imprimirInfo(String.format("Ano letivo atual: %d/%d", anoAtual, anoAtual + 1));
            boolean confirmar = ConsoleUI.lerSimNao(
                    String.format("Avançar para %d/%d?", anoAtual + 1, anoAtual + 2));

            if (confirmar) {
                RepositorioSessao.getInstance().setAnoAtual(anoAtual + 1);
                view.mostrarSucesso(String.format(
                        "Ano letivo avançado para %d/%d.", anoAtual + 1, anoAtual + 2));
            } else {
                view.mostrarCancelamento();
            }
            RepositorioSessao.getInstance().limparSessao(); // termina a sessão do gestor

        } catch (OperacaoCanceladaException e) {
            view.mostrarCancelamento();
        }
    }

    /** Delega no EstudanteController o fluxo de auto-matrícula. */
    public void iniciarAutoMatricula() {
        new EstudanteController().iniciarAutoMatricula();
    }

    /** Fluxo de recuperação de password via email. */
    public void recuperarPassword() {
        try {
            String email = view.pedirEmailRecuperacao();
            OperationResult<Void> res = authBll.recuperarPassword(email);
            if (res.isSuccess()) view.mostrarSucesso(res.getMessage());
            else view.mostrarErro(res.getMessage());
        } catch (OperacaoCanceladaException e) {
            view.mostrarCancelamento();
        }
    }

    // =========================================================================
    // ROUTING INTERNO
    // =========================================================================

    /**
     * Redireciona para o controller do tipo de utilizador autenticado.
     * O tipo é a string devolvida pelo AutenticacaoBLL: "ESTUDANTE", "DOCENTE" ou "GESTOR".
     */
    private void encaminharParaPainel(String tipo) {
        switch (tipo) {
            case "ESTUDANTE" -> new EstudanteController().iniciar();
            case "DOCENTE"   -> new DocenteController().iniciar();
            case "GESTOR"    -> new GestorController().iniciar();
            default          -> view.mostrarErro("Tipo de utilizador desconhecido: " + tipo);
        }
    }
}
