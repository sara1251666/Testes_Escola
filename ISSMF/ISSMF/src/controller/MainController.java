package controller;

import bll.AutenticacaoBLL;
import common.OperacaoCanceladaException;
import common.OperationResult;
import view.MainView;

/**
 * Controller principal — pré-login.
 * Responsabilidades: login, auto-matrícula, recuperação de password,
 * e routing para o painel correto após login.
 */
public class MainController {

    private final AutenticacaoBLL authBll = new AutenticacaoBLL();
    private final MainView        view    = new MainView();

    public void iniciar() {
        boolean aExecutar = true;

        while (aExecutar) {
            try {
                int opcao = view.mostrarMenuPrincipal();
                switch (opcao) {
                    case 1 -> fluxoLogin();
                    case 2 -> new EstudanteController().iniciarAutoMatricula();
                    case 3 -> fluxoRecuperarPassword();
                    case 0 -> {
                        view.mostrarSucesso("A encerrar o sistema. Até breve!");
                        aExecutar = false;
                    }
                    default -> view.mostrarErro("Opção inválida. Escolha entre 0 e 3.");
                }
            } catch (OperacaoCanceladaException e) {
                // No menu principal, o cancelamento é ignorado (não há operação activa)
            }
        }
    }

    // =========================================================================
    // FLUXOS
    // =========================================================================

    private void fluxoLogin() {
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

    private void fluxoRecuperarPassword() {
        try {
            String email = view.pedirEmailRecuperacao();
            OperationResult<Void> res = authBll.recuperarPassword(email);
            if (res.isSuccess()) view.mostrarSucesso(res.getMessage());
            else view.mostrarErro(res.getMessage());
        } catch (OperacaoCanceladaException e) {
            view.mostrarCancelamento();
        }
    }

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
