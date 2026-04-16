import controller.MainController;

/**
 * Ponto de entrada do sistema ISSMF.
 * Instancia o MainController e arranca o loop principal.
 * Toda a lógica de routing, validação e persistência está nas
 * camadas Controller → BLL → DAL → Model.
 */
public class Main {
    public static void main(String[] args) {
        new MainController().iniciar();
    }
}
