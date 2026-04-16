package controller;

import bll.AutenticacaoBLL;
import common.OperacaoCanceladaException;
import common.OperationResult;
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

    // =========================================================================
    // ACÇÕES PÚBLICAS — chamadas pela MainView
    // =========================================================================

    /** Fluxo de autenticação: pede credenciais e redireciona após sucesso. */
    public void fazerLogin() {
        try {
            String[] creds  = view.pedirCredenciais();
            String email    = creds[0];
            String password = creds[1];

            OperationResult<String> res = authBll.fazerLogin(email, password);

            if (res.isSuccess()) {
                view.mostrarSucesso(res.getMessage());
                encaminharParaPainel(res.getData()); // "ESTUDANTE" | "DOCENTE" | "GESTOR"
            } else {
                view.mostrarErro(res.getMessage());
            }

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
