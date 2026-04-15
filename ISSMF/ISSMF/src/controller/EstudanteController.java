package controller;

import bll.EstudanteBLL;
import common.ConsoleUI;
import common.OperacaoCanceladaException;
import common.OperationResult;
import model.Estudante;
import model.RepositorioSessao;
import view.EstudanteView;

public class EstudanteController {
    private final EstudanteBLL bll = new EstudanteBLL();
    private final EstudanteView view = new EstudanteView();

    public void iniciarAutoMatricula() {
        try {
            ConsoleUI.imprimirTitulo("Auto-Matrícula de Novo Estudante");
            String nome = ConsoleUI.lerString("Nome Completo");
            String nif = ConsoleUI.lerString("NIF (9 dígitos)");
            String email = ConsoleUI.lerString("Email Pessoal (ou 'nao' se não tiver)");
            int idCurso = ConsoleUI.lerInteiro("ID do Curso Pretendido");

            OperationResult<Estudante> result = bll.registarAutoMatricula(nome, nif, email, idCurso);

            view.mostrarMensagem(result.getMessage(), result.isSuccess());

        } catch (OperacaoCanceladaException e) {
            view.mostrarMensagem("Processo de Auto-Matrícula cancelado.", false);
        }
    }

    public void iniciar() {
        int idLogado = RepositorioSessao.getInstance().getIdUtilizadorLogado();
        Estudante estudanteLogado = bll.obterEstudante(idLogado);

        if (estudanteLogado == null) {
            ConsoleUI.imprimirErro("Sessão inválida ou corrompida. A regressar ao início.");
            return;
        }

        boolean aExecutar = true;

        while (aExecutar) {
            try {
                int opcao = view.mostrarMenuPrincipal();

                switch (opcao) {
                    case 1:
                        view.mostrarDados(estudanteLogado);
                        break;
                    case 2:
                        atualizarMoradaFlow(estudanteLogado);
                        break;
                    case 3:
                        alterarPasswordFlow(estudanteLogado);
                        break;
                    case 4:
                        pagarPropinasFlow(estudanteLogado);
                        break;
                    case 0: // Logout
                        RepositorioSessao.getInstance().limparSessao();
                        aExecutar = false;
                        break;
                    default:
                        ConsoleUI.imprimirErro("Opção inválida.");
                        ConsoleUI.pausar();
                }
            } catch (OperacaoCanceladaException e) {
            }
        }
    }

    private void atualizarMoradaFlow(Estudante estudante) {
        try {
            ConsoleUI.imprimirTitulo("Atualizar Morada");
            String novaMorada = ConsoleUI.lerString("Insira a nova morada");

            OperationResult<Estudante> resultado = bll.atualizarMorada(estudante, novaMorada);
            view.mostrarMensagem(resultado.getMessage(), resultado.isSuccess());

        } catch (OperacaoCanceladaException e) {
            view.mostrarMensagem("Atualização de morada cancelada.", false);
        }
    }

    private void alterarPasswordFlow(Estudante estudante) {
        try {
            ConsoleUI.imprimirTitulo("Alterar Password");
            String novaPass = ConsoleUI.lerPassword("Insira a nova password");

            OperationResult<Estudante> resultado = bll.alterarPassword(estudante, novaPass);
            view.mostrarMensagem(resultado.getMessage(), resultado.isSuccess());

        } catch (OperacaoCanceladaException e) {
            view.mostrarMensagem("Alteração de password cancelada.", false);
        }
    }

    private void pagarPropinasFlow(Estudante estudante) {
        try {
            ConsoleUI.imprimirTitulo("Estado Financeiro");
            // CORRIGIDO: getSaldoDevedor() em vez de getSaldoPropinas()
            System.out.println("Saldo Devedor Atual: " + estudante.getSaldoDevedor() + "€");

            if (estudante.getSaldoDevedor() > 0) {
                boolean confirmacao = ConsoleUI.lerSimNao("Deseja efetuar o pagamento integral agora?");
                if (confirmacao) {
                    OperationResult<Estudante> resultado = bll.pagarPropinas(estudante);
                    view.mostrarMensagem(resultado.getMessage(), resultado.isSuccess());
                }
            } else {
                ConsoleUI.pausar();
            }
        } catch (OperacaoCanceladaException e) {
            view.mostrarMensagem("Processo de pagamento cancelado.", false);
        }
    }
}