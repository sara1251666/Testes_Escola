package controller;

import bll.EstudanteBLL;
import common.ConsoleUI;
import common.OperacaoCanceladaException;
import common.OperationResult;
import common.ValidacaoUtil;
import model.Avaliacao;
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
            String nif        = ValidacaoUtil.pedirNifValido();
            String email      = ConsoleUI.lerString("Email pessoal (ou 'nao' se não tiver)");
            String dataNasc = ConsoleUI.lerData("Data de Nascimento (dd-MM-yyyy)");
            String morada     = ConsoleUI.lerString("Morada");
            String siglaCurso = ConsoleUI.lerString("Sigla do Curso pretendido").toUpperCase();

            int anoAtual = RepositorioSessao.getInstance().getAnoAtual();
            OperationResult<Estudante> res = bll.registar(nome, nif, email, dataNasc, morada, siglaCurso, anoAtual);
            if (res.isSuccess()) view.mostrarSucesso(res.getMessage());
            else view.mostrarErro(res.getMessage());

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
                    case 2 -> fluxoVerNotas(estudante);
                    case 3 -> fluxoAtualizarMorada(estudante);
                    case 4 -> fluxoAlterarPassword(estudante);
                    case 5 -> fluxoPropinas(estudante);
                    case 6 -> fluxoTransitarAno(estudante);
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

    private void fluxoVerNotas(Estudante estudante) {
        int anoAtual = RepositorioSessao.getInstance().getAnoAtual();
        OperationResult<List<Avaliacao>> res = bll.obterNotas(estudante.getNumMec(), anoAtual);
        if (res.isSuccess()) {
            view.mostrarNotas(res.getData());
        } else {
            view.mostrarErro(res.getMessage());
        }
    }

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

}
