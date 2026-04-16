package view;

import common.ConsoleUI;

/**
 * View do ecrã principal (pré-login).
 */
public class MainView {

    public int mostrarMenuPrincipal() {
        ConsoleUI.imprimirCabecalho("Bem-vindo ao ISSMF");
        ConsoleUI.imprimirMenu(new String[]{
                "Fazer Login",
                "Auto-Matrícula (Novos Estudantes)",
                "Recuperar Password"
        });
        return ConsoleUI.lerOpcaoMenu("Selecione uma opção");
    }

    public String[] pedirCredenciais() {
        ConsoleUI.imprimirTitulo("Acesso ao Sistema");
        String email    = ConsoleUI.lerString("Email");
        String password = ConsoleUI.lerPassword("Password");
        return new String[]{email, password};
    }

    public String pedirEmailRecuperacao() {
        ConsoleUI.imprimirTitulo("Recuperar Password");
        return ConsoleUI.lerString("Email da conta a recuperar");
    }

    public void mostrarSucesso(String msg) {
        ConsoleUI.imprimirSucesso(msg);
        ConsoleUI.pausar();
    }

    public void mostrarErro(String msg) {
        ConsoleUI.imprimirErro(msg);
        ConsoleUI.pausar();
    }

    public void mostrarCancelamento() {
        ConsoleUI.imprimirInfo("Operação cancelada.");
        ConsoleUI.pausar();
    }
}
