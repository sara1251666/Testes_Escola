package controller;

import bll.AutenticacaoBLL;
import common.ConsoleUI;
import common.OperacaoCanceladaException;
import common.OperationResult;
import view.MainView;

public class MainController {
    private final AutenticacaoBLL authBll;

    public MainController() {
        this.authBll = new AutenticacaoBLL();
    }

    public void fluxoLogin(MainView view) {
        try {
            ConsoleUI.imprimirTitulo("Acesso ao Sistema");
            String email = ConsoleUI.lerString("Email");
            String password = ConsoleUI.lerPassword("Password");

            OperationResult<String> resultado = authBll.fazerLogin(email, password);

            if (resultado.isSuccess()) {
                view.mostrarMensagem(resultado.getMessage(), true);

                encaminharParaPainel(resultado.getData());
            } else {
                view.mostrarMensagem(resultado.getMessage(), false);
            }
        } catch (OperacaoCanceladaException e) {
            view.mostrarMensagem("Login cancelado.", false);
        }
    }

    public void fluxoAutoMatricula() {
        new EstudanteController().iniciarAutoMatricula();
    }

    public void fluxoRecuperarPassword(MainView view) {
        try {
            ConsoleUI.imprimirTitulo("Recuperar Password");
            String email = ConsoleUI.lerString("Insira o Email para recuperação");

            OperationResult<Void> resultado = authBll.recuperarPassword(email);
            view.mostrarMensagem(resultado.getMessage(), resultado.isSuccess());

        } catch (OperacaoCanceladaException e) {
            view.mostrarMensagem("Operação cancelada.", false);
        }
    }

    private void encaminharParaPainel(String tipoPerfil) {
        if ("ESTUDANTE".equals(tipoPerfil)) {
            new EstudanteController().iniciar();
        } else if ("DOCENTE".equals(tipoPerfil)) {
            new DocenteController().iniciar();
        } else if ("GESTOR".equals(tipoPerfil)) {
            new GestorController().iniciar();
        }
    }
}