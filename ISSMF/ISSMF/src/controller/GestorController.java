package controller;

import bll.EstudanteBLL;
import bll.GestorBLL;
import common.OperacaoCanceladaException;
import common.OperationResult;
import model.RepositorioSessao;
import view.GestorView;

/**
 * Controller do Painel de Gestão.
 */
public class GestorController {

    private final GestorView   view         = new GestorView();
    private final GestorBLL    bll          = new GestorBLL();
    private final EstudanteBLL estudanteBLL = new EstudanteBLL();

    public void iniciar() {
        boolean ativo = true;
        while (ativo) {
            try {
                int opcao = view.mostrarMenuPrincipal();
                switch (opcao) {
                    case 1 -> fluxoRegistarEstudante();
                    case 2 -> fluxoRegistarDocente();
                    case 3 -> fluxoGerirCursos();
                    case 4 -> fluxoCriarUC();
                    case 5 -> fluxoVerificarQuorum();
                    case 6 -> fluxoEstatisticas();
                    case 7 -> fluxoDevedores();
                    case 8 -> fluxoAlterarPassword();
                    case 0 -> { RepositorioSessao.getInstance().limparSessao(); ativo = false; }
                    default -> view.mostrarErro("Opção inválida.");
                }
            } catch (OperacaoCanceladaException e) {
                view.mostrarCancelamento();
            }
        }
    }

    private void fluxoRegistarEstudante() {
        try {
            String[] d = view.pedirDadosEstudante();
            int anoAtual = RepositorioSessao.getInstance().getAnoAtual();
            var res = estudanteBLL.registar(d[0], d[1], d[2], d[3], d[4], d[5], anoAtual);
            if (res.isSuccess()) view.mostrarSucesso(res.getMessage());
            else view.mostrarErro(res.getMessage());
        } catch (OperacaoCanceladaException e) { view.mostrarCancelamento(); }
    }

    private void fluxoRegistarDocente() {
        try {
            String[] d = view.pedirDadosDocente();
            OperationResult<?> res = bll.registarDocente(d[0], d[1], d[2], d[3]);
            if (res.isSuccess()) view.mostrarSucesso(res.getMessage());
            else view.mostrarErro(res.getMessage());
        } catch (OperacaoCanceladaException e) { view.mostrarCancelamento(); }
    }

    private void fluxoGerirCursos() {
        boolean subMenu = true;
        while (subMenu) {
            try {
                int opcao = view.mostrarSubMenuCursos();
                switch (opcao) {
                    case 1 -> {
                        String[] d = view.pedirDadosCurso();
                        var res = bll.criarCurso(d[0], d[1], d[2], Double.parseDouble(d[3]));
                        if (res.isSuccess()) view.mostrarSucesso(res.getMessage());
                        else view.mostrarErro(res.getMessage());
                    }
                    case 2 -> {
                        String[] d = view.pedirDadosEdicaoCurso();
                        var res = bll.editarCurso(d[0], d[1], d[2], Double.parseDouble(d[3]));
                        if (res.isSuccess()) view.mostrarSucesso(res.getMessage());
                        else view.mostrarErro(res.getMessage());
                    }
                    case 0 -> subMenu = false;
                    default -> view.mostrarErro("Opção inválida.");
                }
            } catch (OperacaoCanceladaException e) { subMenu = false; }
        }
    }

    private void fluxoCriarUC() {
        try {
            String[] d = view.pedirDadosUC();
            // d: [sigla, nome, curso, ano, docente]
            var res = bll.criarUC(d[0], d[1], d[2], Integer.parseInt(d[3]), d[4]);
            if (res.isSuccess()) view.mostrarSucesso(res.getMessage());
            else view.mostrarErro(res.getMessage());
        } catch (OperacaoCanceladaException e) { view.mostrarCancelamento(); }
    }

    private void fluxoVerificarQuorum() {
        try {
            String[] d = view.pedirDadosQuorum();
            var res = bll.verificarQuorum(d[0], Integer.parseInt(d[1]));
            if (res.isSuccess()) view.mostrarSucesso(res.getMessage());
            else view.mostrarErro(res.getMessage());
        } catch (OperacaoCanceladaException e) { view.mostrarCancelamento(); }
    }

    private void fluxoEstatisticas() {
        OperationResult<String> res = bll.obterEstatisticasGlobais();
        view.mostrarRelatorio("Estatísticas Globais", res.getData() != null ? res.getData() : res.getMessage());
    }

    private void fluxoDevedores() {
        OperationResult<String> res = bll.obterRelatorioDevedores();
        if (res.isSuccess()) view.mostrarRelatorio("Alunos com Propinas em Dívida", res.getData());
        else view.mostrarSucesso(res.getMessage());
    }

    private void fluxoAlterarPassword() {
        try {
            String email = RepositorioSessao.getInstance().getEmailUtilizadorLogado();
            String novaPass = view.pedirNovaPassword();
            var res = bll.alterarPassword(email, novaPass);
            if (res.isSuccess()) view.mostrarSucesso(res.getMessage());
            else view.mostrarErro(res.getMessage());
        } catch (OperacaoCanceladaException e) { view.mostrarCancelamento(); }
    }
}
