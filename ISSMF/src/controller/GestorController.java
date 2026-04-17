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
                    case 1  -> fluxoRegistarEstudante();
                    case 2  -> fluxoRegistarDocente();
                    case 3  -> fluxoGerirDepartamentos();
                    case 4  -> fluxoGerirCursos();
                    case 5  -> fluxoCriarUC();
                    case 6  -> fluxoVerificarQuorum();
                    case 7  -> fluxoEstatisticas();
                    case 8  -> fluxoDevedores();
                    case 9  -> fluxoGerirEstadoUtilizadores();
                    case 10 -> fluxoAlterarPassword();
                    case 0  -> { RepositorioSessao.getInstance().limparSessao(); ativo = false; }
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

    private void fluxoGerirDepartamentos() {
        boolean sub = true;
        while (sub) {
            try {
                int opcao = view.mostrarSubMenuDepartamentos();
                switch (opcao) {
                    case 1 -> {
                        String[] d = view.pedirDadosDepartamento();
                        var res = bll.registarDepartamento(d[0], d[1]);
                        if (res.isSuccess()) view.mostrarSucesso(res.getMessage());
                        else view.mostrarErro(res.getMessage());
                    }
                    case 2 -> {
                        var res = bll.listarDepartamentos();
                        view.mostrarRelatorio("Departamentos", res.getData() != null ? res.getData() : res.getMessage());
                    }
                    case 0 -> sub = false;
                    default -> view.mostrarErro("Opção inválida.");
                }
            } catch (OperacaoCanceladaException e) { sub = false; }
        }
    }

    private void fluxoRegistarDocente() {
        try {
            String[] d = view.pedirDadosDocente();
            // d = [nome, nif, dataNasc, morada]  →  BLL espera (nome, nif, morada, dataNascimento)
            OperationResult<?> res = bll.registarDocente(d[0], d[1], d[3], d[2]);
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

    private void fluxoGerirEstadoUtilizadores() {
        boolean sub = true;
        while (sub) {
            try {
                int opcao = view.mostrarSubMenuEstadoUtilizadores();
                switch (opcao) {
                    case 1 -> {
                        // Listar estudantes e permitir ativar/desativar
                        OperationResult<String> lista = bll.listarEstudantesComEstado();
                        view.mostrarRelatorio("Estudantes", lista.isSuccess() ? lista.getData() : lista.getMessage());
                        if (!lista.isSuccess()) break;
                        int numMec = view.pedirNumMecEstudante();
                        boolean novoEstado = view.pedirNovoEstado("Estudante nº " + numMec);
                        var res = bll.alterarEstadoEstudante(numMec, novoEstado);
                        if (res.isSuccess()) view.mostrarSucesso(res.getMessage());
                        else view.mostrarErro(res.getMessage());
                    }
                    case 2 -> {
                        // Listar docentes e permitir ativar/desativar
                        OperationResult<String> lista = bll.listarDocentesComEstado();
                        view.mostrarRelatorio("Docentes", lista.isSuccess() ? lista.getData() : lista.getMessage());
                        if (!lista.isSuccess()) break;
                        String sigla = view.pedirSiglaDocente();
                        boolean novoEstado = view.pedirNovoEstado("Docente " + sigla);
                        var res = bll.alterarEstadoDocente(sigla, novoEstado);
                        if (res.isSuccess()) view.mostrarSucesso(res.getMessage());
                        else view.mostrarErro(res.getMessage());
                    }
                    case 0 -> sub = false;
                    default -> view.mostrarErro("Opção inválida.");
                }
            } catch (OperacaoCanceladaException e) { sub = false; }
        }
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
