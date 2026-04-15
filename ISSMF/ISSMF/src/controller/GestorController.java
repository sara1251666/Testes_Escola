package controller;

import bll.EstudanteBLL;
import bll.GestorBLL;
import common.ConsoleUI;
import common.OperacaoCanceladaException;
import common.OperationResult;
import model.Estudante;
import model.RepositorioSessao;
import view.GestorView;

public class GestorController {
    private final GestorView view = new GestorView();
    private final GestorBLL bll = new GestorBLL();
    private final int idGestorLogado;

    public GestorController() {
        this.idGestorLogado = RepositorioSessao.getInstance().getIdUtilizadorLogado();
    }

    public void iniciar() {
        boolean aExecutar = true;
        while (aExecutar) {
            try {
                int opcao = view.mostrarMenuPrincipal();
                switch (opcao) {
                    case 1: fluxoRegistarNovoEstudante(); break;
                    case 2: fluxoGerirUcs(); break;
                    case 3: fluxoGerirCursos(); break;
                    case 4: fluxoVerEstatisticas(); break;
                    case 5: fluxoAvancarAnoLetivo(); break;
                    case 6: fluxoListarDevedores(); break;
                    case 7: fluxoAlterarPassword(); break;
                    case 0: aExecutar = false; break;
                }
            } catch (OperacaoCanceladaException e) {
                view.mostrarMensagem("Operação cancelada.", false);
            }
        }
    }

    private final EstudanteBLL estudanteBLL = new EstudanteBLL();

    private void fluxoRegistarNovoEstudante() {
        try {
            ConsoleUI.imprimirTitulo("Registar Novo Estudante (Secretaria)");

            String nome = ConsoleUI.lerString("Nome Completo");
            String nif = ConsoleUI.lerString("NIF (9 dígitos)");
            String email = ConsoleUI.lerString("Email do Estudante");
            int idCurso = ConsoleUI.lerInteiro("ID do Curso");

            OperationResult<Estudante> result = estudanteBLL.registarAutoMatricula(nome, nif, email, idCurso);

            view.mostrarMensagem(result.getMessage(), result.isSuccess());

        } catch (OperacaoCanceladaException e) {
            view.mostrarMensagem("Processo de registo cancelado pelo Gestor.", false);
        }
    }

    private void fluxoGerirUcs() throws OperacaoCanceladaException {
        ConsoleUI.imprimirCabecalho("Gestão de Unidades Curriculares");
        String sigla = ConsoleUI.lerString("Sigla da UC");
        String nome = ConsoleUI.lerString("Nome da UC");
        String curso = ConsoleUI.lerString("Sigla do Curso");
        int ano = ConsoleUI.lerInteiro("Ano Curricular (1-3)");
        int idDocente = ConsoleUI.lerInteiro("ID do Docente Responsável");

        OperationResult<Void> res = bll.criarUc(sigla, nome, curso, ano, idDocente);
        view.mostrarMensagem(res.getMessage(), res.isSuccess());
    }

    private void fluxoGerirCursos() {
        boolean noSubMenu = true;
        while (noSubMenu) {
            try {
                int opcao = view.mostrarSubMenuCursos();
                switch (opcao) {
                    case 1: adicionarCursoFlow(); break;
                    case 2: editarCursoFlow(); break;
                    case 0: noSubMenu = false; break;
                    default: ConsoleUI.imprimirErro("Opção inválida."); ConsoleUI.pausar();
                }
            } catch (OperacaoCanceladaException e) {
                noSubMenu = false; // Sai do submenu com '0'
            }
        }
    }

    private void adicionarCursoFlow() throws OperacaoCanceladaException {
        String sigla = ConsoleUI.lerString("Sigla do Curso");
        String nome = ConsoleUI.lerString("Nome do Curso");
        String dep = ConsoleUI.lerString("Departamento");

        OperationResult<Void> res = bll.criarCurso(sigla, nome, dep);
        view.mostrarMensagem(res.getMessage(), res.isSuccess());
    }

    private void editarCursoFlow() throws OperacaoCanceladaException {
        String sigla = ConsoleUI.lerString("Sigla do Curso a editar");
        OperationResult<Boolean> validacao = bll.verificarSePodeEditarCurso(sigla);

        if (!validacao.isSuccess()) {
            view.mostrarMensagem(validacao.getMessage(), false);
            return;
        }

        String novoNome = ConsoleUI.lerString("Novo Nome");
        String novoDep = ConsoleUI.lerString("Novo Departamento");
        OperationResult<Void> res = bll.editarCurso(sigla, novoNome, novoDep);
        view.mostrarMensagem(res.getMessage(), res.isSuccess());
    }

    private void fluxoVerEstatisticas() {
        OperationResult<String> res = bll.obterEstatisticasGlobais();
        view.mostrarRelatorio("Estatísticas Globais do Sistema", res.getData());
    }

    private void fluxoAvancarAnoLetivo() {
        try {
            ConsoleUI.imprimirTitulo("Avançar Ano Letivo");
            String confirmacao = ConsoleUI.lerString("CUIDADO: Tem a certeza que deseja transitar todos os alunos de ano? (S/N)");
            if (confirmacao.equalsIgnoreCase("S")) {
                OperationResult<Void> res = bll.avancarAnoLetivo();
                view.mostrarMensagem(res.getMessage(), res.isSuccess());
            }
        } catch (OperacaoCanceladaException e) {
            view.mostrarMensagem("Transição de ano letivo abortada.", false);
        }
    }

    private void fluxoListarDevedores() {
        OperationResult<String> resultado = bll.obterRelatorioDividas();
        if (resultado.isSuccess()) {
            view.mostrarRelatorio("Alunos com Propinas em Dívida", resultado.getData());
        } else {
            view.mostrarMensagem(resultado.getMessage(), false);
        }
    }

    private void fluxoAlterarPassword() {
        try {
            ConsoleUI.imprimirTitulo("Alterar Password do Gestor");
            String novaPass = ConsoleUI.lerPassword("Nova Password");
            OperationResult<Void> res = bll.alterarPassword(idGestorLogado, novaPass);
            view.mostrarMensagem(res.getMessage(), res.isSuccess());
        } catch (OperacaoCanceladaException e) {
            view.mostrarMensagem("Operação cancelada.", false);
        }
    }
}