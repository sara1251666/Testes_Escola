package view;

import common.ConsoleUI;
import common.OperacaoCanceladaException;

public class DocenteView {

    public int mostrarMenuPrincipal() throws OperacaoCanceladaException {
        ConsoleUI.imprimirCabecalho("Portal do Docente");
        System.out.println("1 - Consultar os Meus Alunos e Estatísticas");
        System.out.println("2 - Lançar Notas");
        System.out.println("3 - Alterar Password");
        System.out.println("0 - Sair / Logout");
        System.out.println("--------------------------------------------------");
        return ConsoleUI.lerInteiro("Escolha uma opção");
    }

    public void mostrarEstatisticas(String relatorio) {
        ConsoleUI.imprimirTitulo("Os Meus Alunos e Estatísticas");
        System.out.println(relatorio);
        ConsoleUI.pausar();
    }

    public void mostrarMensagem(String mensagem, boolean sucesso) {
        if (sucesso) {
            ConsoleUI.imprimirSucesso(mensagem);
        } else {
            ConsoleUI.imprimirErro(mensagem);
        }
        ConsoleUI.pausar();
    }
}