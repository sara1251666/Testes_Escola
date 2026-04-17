package view;

import common.ConsoleUI;
import model.Docente;

/**
 * View do Portal do Docente.
 * Apenas I/O visual — sem lógica de negócio.
 */
public class DocenteView {

    public int mostrarMenuPrincipal() {
        ConsoleUI.imprimirCabecalho("Portal do Docente");
        ConsoleUI.imprimirMenu(new String[]{
                "Consultar a Minha Ficha",
                "Ver Alunos e Notas das Minhas UCs",
                "Lançar / Atualizar Notas",
                "Alterar Password"
        });
        return ConsoleUI.lerOpcaoMenu("Escolha uma opção");
    }

    public void mostrarFicha(Docente d) {
        ConsoleUI.imprimirTitulo("A Minha Ficha");
        System.out.printf("  Sigla            : %s%n", d.getSigla());
        System.out.printf("  Nome             : %s%n", d.getNome());
        System.out.printf("  Email            : %s%n", d.getEmail());
        System.out.printf("  NIF              : %s%n", d.getNif());
        System.out.printf("  Data Nascimento  : %s%n", d.getDataNascimento());
        System.out.printf("  Morada           : %s%n", d.getMorada());
        ConsoleUI.pausar();
    }

    public void mostrarEstatisticas(String relatorio) {
        ConsoleUI.imprimirTitulo("Os Meus Alunos e UCs");
        System.out.println(relatorio);
        ConsoleUI.pausar();
    }

    public void mostrarMensagem(String mensagem, boolean sucesso) {
        if (sucesso) ConsoleUI.imprimirSucesso(mensagem);
        else         ConsoleUI.imprimirErro(mensagem);
        ConsoleUI.pausar();
    }
}
