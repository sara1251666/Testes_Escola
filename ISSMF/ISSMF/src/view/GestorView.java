package view;

import common.ConsoleUI;
import common.OperacaoCanceladaException;

public class GestorView {

    public int mostrarMenuPrincipal() throws OperacaoCanceladaException {
            ConsoleUI.imprimirCabecalho("Painel de Gestão ISSMF");
            System.out.println("1 - Registar Novo Estudante");
            System.out.println("2 - Gerir Unidades Curriculares");
            System.out.println("3 - Gerir Cursos");
            System.out.println("4 - Ver Estatísticas");
            System.out.println("5 - Avançar Ano Letivo");
            System.out.println("6 - Listar Devedores");
            System.out.println("7 - Alterar Password");
            System.out.println("0 - Sair / Logout");

            return ConsoleUI.lerInteiro("Opção");
    }

    public int mostrarSubMenuCursos() throws OperacaoCanceladaException {
        ConsoleUI.imprimirTitulo("Gestão de Cursos");
        System.out.println("1 - Adicionar Novo Curso");
        System.out.println("2 - Editar Curso Existente");
        System.out.println("0 - Voltar ao Menu Principal");
        return ConsoleUI.lerInteiro("Escolha uma opção");
    }

    public void mostrarRelatorio(String titulo, String relatorio) {
        ConsoleUI.imprimirTitulo(titulo);
        System.out.println(relatorio);
        ConsoleUI.pausar();
    }

    public void mostrarMensagem(String msg, boolean sucesso) {
        if (sucesso) ConsoleUI.imprimirSucesso(msg);
        else ConsoleUI.imprimirErro(msg);
        ConsoleUI.pausar();
    }
}