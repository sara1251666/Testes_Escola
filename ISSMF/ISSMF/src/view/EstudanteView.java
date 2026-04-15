package view;

import common.ConsoleUI;
import common.OperacaoCanceladaException;
import model.Estudante;

public class EstudanteView {

    public int mostrarMenuPrincipal() throws OperacaoCanceladaException {
        ConsoleUI.imprimirCabecalho("Portal do Estudante");
        System.out.println("1. Consultar Dados Pessoais");
        System.out.println("2. Atualizar Morada");
        System.out.println("3. Alterar Password");
        System.out.println("4. Consultar Estado Financeiro");
        System.out.println("0. Sair / Logout");
        ConsoleUI.imprimirLinha();
        return ConsoleUI.lerInteiro("Selecione uma opção");
    }

    public void mostrarDados(Estudante estudante) {
        ConsoleUI.imprimirTitulo("Os Seus Dados Pessoais");

        System.out.println("Nº Mecanográfico : " + estudante.getNumMec());
        System.out.println("Nome Completo    : " + estudante.getNome());
        System.out.println("NIF              : " + estudante.getNif());
        System.out.println("Email            : " + estudante.getEmail());
        System.out.println("Morada           : " + estudante.getMorada());
        System.out.println("ID do Curso      : " + estudante.getIdCurso());
        System.out.println("Saldo Devedor    : " + estudante.getSaldoDevedor() + "€");

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

    public void mostrarEcraAutoMatricula() {
        ConsoleUI.imprimirCabecalho("Portal de Auto-Matrícula");
        System.out.println("Por favor, introduza os seus dados para efetuar a matrícula no ISSMF.");
        ConsoleUI.imprimirLinha();
    }

    public void mostrarOperacaoCancelada() {
        ConsoleUI.imprimirErro("A operação de auto-matrícula foi cancelada. Nenhum dado foi guardado.");
        ConsoleUI.pausar();
    }

}