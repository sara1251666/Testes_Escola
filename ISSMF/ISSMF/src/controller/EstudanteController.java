package controller;

import bll.EstudanteBLL;
import common.ConsoleUI;
import common.OperacaoCanceladaException;
import common.OperationResult;
import model.Estudante;
import model.Pagamento;
import model.RepositorioSessao;
import view.EstudanteView;

import java.util.List;

/**
 * Controller do Portal do Estudante.
 *
 * Orquestra todos os fluxos do estudante: consulta, morada,
 * password, propinas (parcial/total), transição de ano.
 */
public class EstudanteController {

    private final EstudanteBLL bll  = new EstudanteBLL();
    private final EstudanteView view = new EstudanteView();

    // =========================================================================
    // AUTO-MATRÍCULA (sem sessão ativa)
    // =========================================================================

    /** Fluxo de auto-matrícula para novos estudantes. */
    public void iniciarAutoMatricula() {
        try {
            view.mostrarCabecalhoAutoMatricula();

            String nome       = ConsoleUI.lerString("Nome Completo");
            String nif        = pedirNifValido();
            String email      = ConsoleUI.lerString("Email pessoal (ou 'nao' se não tiver)");
            String dataNasc   = pedirDataValida("Data de Nascimento (dd-MM-yyyy)");
            String morada     = ConsoleUI.lerString("Morada");
            String siglaCurso = ConsoleUI.lerString("Sigla do Curso pretendido").toUpperCase();

            int anoAtual = RepositorioSessao.getInstance().getAnoAtual();
            OperationResult<Estudante> res = bll.registar(nome, nif, email, dataNasc, morada, siglaCurso, anoAtual);
            view.mostrarSucesso(res.getMessage());
            if (!res.isSuccess()) view.mostrarErro(res.getMessage());

        } catch (OperacaoCanceladaException e) {
            view.mostrarCancelamento();
        }
    }

    // =========================================================================
    // PORTAL DO ESTUDANTE (com sessão ativa)
    // =========================================================================

    public void iniciar() {
        int numMec = RepositorioSessao.getInstance().getIdUtilizadorLogado();
        Estudante estudante = bll.obterPorId(numMec);

        if (estudante == null) {
            ConsoleUI.imprimirErro("Sessão inválida. Por favor faça login novamente.");
            return;
        }

        boolean ativo = true;
        while (ativo) {
            try {
                int opcao = view.mostrarMenuPrincipal();
                switch (opcao) {
                    case 1 -> view.mostrarDadosPessoais(estudante);
                    case 2 -> fluxoAtualizarMorada(estudante);
                    case 3 -> fluxoAlterarPassword(estudante);
                    case 4 -> fluxoPropinas(estudante);
                    case 5 -> fluxoTransitarAno(estudante);
                    case 0 -> { RepositorioSessao.getInstance().limparSessao(); ativo = false; }
                    default -> view.mostrarErro("Opção inválida.");
                }
            } catch (OperacaoCanceladaException e) {
                view.mostrarCancelamento();
            }
        }
    }

    // =========================================================================
    // FLUXOS PRIVADOS
    // =========================================================================

    private void fluxoAtualizarMorada(Estudante estudante) {
        try {
            String novaMorada = view.pedirNovaMorada();
            OperationResult<Estudante> res = bll.atualizarMorada(estudante, novaMorada);
            if (res.isSuccess()) view.mostrarSucesso(res.getMessage());
            else view.mostrarErro(res.getMessage());
        } catch (OperacaoCanceladaException e) {
            view.mostrarCancelamento();
        }
    }

    private void fluxoAlterarPassword(Estudante estudante) {
        try {
            String novaPass = view.pedirNovaPassword();
            OperationResult<Void> res = bll.alterarPassword(estudante.getEmail(), novaPass);
            if (res.isSuccess()) view.mostrarSucesso(res.getMessage());
            else view.mostrarErro(res.getMessage());
        } catch (OperacaoCanceladaException e) {
            view.mostrarCancelamento();
        }
    }

    private void fluxoPropinas(Estudante estudante) {
        try {
            int anoAtual = RepositorioSessao.getInstance().getAnoAtual();

            // Mostrar estado financeiro + histórico
            OperationResult<List<Pagamento>> histRes =
                    bll.obterHistoricoPagamentos(estudante.getNumMec(), anoAtual);
            List<Pagamento> historico = histRes.isSuccess() ? histRes.getData() : List.of();
            view.mostrarEstadoFinanceiro(estudante, historico);

            if (estudante.getSaldoDevedor() <= 0) {
                ConsoleUI.imprimirSucesso("Propina liquidada. Sem dívidas.");
                ConsoleUI.pausar();
                return;
            }

            double valorPago = view.pedirValorPagamento(estudante.getSaldoDevedor());

            OperationResult<Estudante> res = bll.pagarPropina(estudante, valorPago, anoAtual);
            if (res.isSuccess()) {
                estudante = res.getData(); // atualizar referência local
                view.mostrarSucesso(res.getMessage());
            } else {
                view.mostrarErro(res.getMessage());
            }

        } catch (OperacaoCanceladaException e) {
            view.mostrarCancelamento();
        }
    }

    private void fluxoTransitarAno(Estudante estudante) {
        try {
            if (!view.confirmarTransicaoAno(estudante.getAnoCurricular())) {
                view.mostrarCancelamento();
                return;
            }
            int anoAtual = RepositorioSessao.getInstance().getAnoAtual();
            OperationResult<Estudante> res = bll.transitarAno(estudante, anoAtual);
            if (res.isSuccess()) view.mostrarSucesso(res.getMessage());
            else view.mostrarErro(res.getMessage());
        } catch (OperacaoCanceladaException e) {
            view.mostrarCancelamento();
        }
    }

    // =========================================================================
    // VALIDAÇÕES DE INPUT (reutilizadas em auto-matrícula)
    // =========================================================================

    private String pedirNifValido() {
        while (true) {
            String nif = ConsoleUI.lerString("NIF (9 dígitos)");
            if (nif.matches("\\d{9}")) return nif;
            ConsoleUI.imprimirErro("NIF inválido. Deve ter exatamente 9 dígitos numéricos.");
        }
    }

    private String pedirDataValida(String prompt) {
        while (true) {
            String data = ConsoleUI.lerString(prompt);
            if (data.matches("\\d{2}-\\d{2}-\\d{4}")) return data;
            ConsoleUI.imprimirErro("Formato inválido. Use dd-MM-yyyy (ex: 01-01-2000).");
        }
    }
}
