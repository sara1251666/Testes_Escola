package view;

import common.ConsoleUI;
import common.OperacaoCanceladaException;
import controller.MainController;

/**
 * View do ecrã principal (pré-login).
 *
 * É responsável pelo loop de interacção e por chamar o MainController
 * para cada acção escolhida pelo utilizador.
 * Cadeia: Main → MainView.iniciar() → MainController
 */
public class MainView {

    /**
     * Arranca o loop principal da aplicação.
     * Cria o MainController passando-se a si própria como view de referência.
     */
    public void iniciar() {
        MainController controller = new MainController(this);
        boolean aExecutar = true;

        while (aExecutar) {
            try {
                int opcao = mostrarMenuPrincipal();
                switch (opcao) {
                    case 1 -> controller.fazerLogin();
                    case 2 -> controller.iniciarAutoMatricula();
                    case 3 -> controller.recuperarPassword();
                    case 0 -> {
                        mostrarSucesso("A encerrar o sistema. Até breve!");
                        aExecutar = false;
                    }
                    default -> mostrarErro("Opção inválida. Escolha entre 0 e 3.");
                }
            } catch (OperacaoCanceladaException e) {
                // No menu principal o cancelamento é ignorado
            }
        }
    }

    // =========================================================================
    // MENUS E ECRÃS
    // =========================================================================

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
        ConsoleUI.imprimirDicaFormulario();
        String email    = ConsoleUI.lerString("Email");
        String password = ConsoleUI.lerPassword("Password");
        return new String[]{email, password};
    }

    public String pedirEmailRecuperacao() {
        ConsoleUI.imprimirTitulo("Recuperar Password");
        ConsoleUI.imprimirDicaFormulario();
        return ConsoleUI.lerString("Email da conta a recuperar");
    }

    // =========================================================================
    // FEEDBACK
    // =========================================================================

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
