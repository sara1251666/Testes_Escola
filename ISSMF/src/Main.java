import view.MainView;

/**
 * Ponto de entrada do sistema ISSMF.
 * Delega imediatamente para a MainView, que orquestra o fluxo
 * pré-login chamando o MainController.
 * Cadeia: Main → MainView → MainController → BLL → DAL → Model
 */
public class Main {
    public static void main(String[] args) {
        new MainView().iniciar();
    }
}
