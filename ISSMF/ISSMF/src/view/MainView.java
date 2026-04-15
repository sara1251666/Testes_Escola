package view;

import controller.MainController;
import common.ConsoleUI;
import common.OperacaoCanceladaException;

public class MainView {
    private final MainController controller;

    public MainView() {
        this.controller = new MainController();
    }

    public void iniciar() {
        boolean aExecutar = true;

        while (aExecutar) {
            try {
                ConsoleUI.imprimirCabecalho("Bem-vindo ao ISSMF");
                System.out.println("1 - Fazer Login");
                System.out.println("2 - Auto-Matrícula (Novos Estudantes)");
                System.out.println("3 - Recuperar Password");
                System.out.println("0 - Sair do Sistema");
                System.out.println("--------------------------------------------------");

                int opcao = ConsoleUI.lerInteiro("Selecione uma opção");

                switch (opcao) {
                    case 1:
                        controller.fluxoLogin(this);
                        break;
                    case 2:
                        controller.fluxoAutoMatricula();
                        break;
                    case 3:
                        controller.fluxoRecuperarPassword(this);
                        break;
                    case 0:
                        ConsoleUI.imprimirSucesso("A encerrar o sistema. Até breve!");
                        aExecutar = false;
                        break;
                    default:
                        mostrarMensagem("Opção inválida. Escolha entre 0 e 3.", false);
                }
            } catch (OperacaoCanceladaException e) {
            }
        }
    }

    public void mostrarMensagem(String msg, boolean sucesso) {
        if (sucesso) {
            ConsoleUI.imprimirSucesso(msg);
        } else {
            ConsoleUI.imprimirErro(msg);
        }
        ConsoleUI.pausar();
    }
}