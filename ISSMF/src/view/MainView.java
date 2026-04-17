package view;

import common.ConsoleUI;
import common.OperacaoCanceladaException;
import controller.MainController;
import dal.CursoDAL;
import dal.UcDAL;
import model.Curso;
import model.RepositorioSessao;
import model.UC;

import java.util.List;

/**
 * View do ecrã principal (pré-login).
 *
 * É responsável pelo loop de interacção e por chamar o MainController
 * para cada acção escolhida pelo utilizador.
 * Cadeia: Main → MainView.iniciar() → MainController
 */
public class MainView {

    /**
     * Arranca o loop principal da aplicação.
     * Cria o MainController passando-se a si própria como view de referência.
     */
    public void iniciar() {
        MainController controller = new MainController(this);
        boolean aExecutar = true;

        while (aExecutar) {
            try {
                int opcao = mostrarMenuPrincipal();
                switch (opcao) {
                    case 1 -> controller.fazerLogin();
                    case 2 -> controller.iniciarAutoMatricula();
                    case 3 -> controller.recuperarPassword();
                    case 4 -> mostrarAtalhosConsulta();
                    case 5 -> controller.avancarAnoLetivo();
                    case 0 -> {
                        mostrarSucesso("A encerrar o sistema. Até breve!");
                        aExecutar = false;
                    }
                    default -> mostrarErro("Opção inválida. Escolha entre 0 e 5.");
                }
            } catch (OperacaoCanceladaException e) {
                // No menu principal o cancelamento é ignorado
            }
        }
    }

    // =========================================================================
    // MENUS E ECRÃS
    // =========================================================================

    public int mostrarMenuPrincipal() {
        ConsoleUI.imprimirCabecalho("Bem-vindo ao ISSMF");
        ConsoleUI.imprimirMenu(new String[]{
                "Fazer Login",
                "Auto-Matrícula (Novos Estudantes)",
                "Recuperar Password",
                "Consultar Informação do Sistema",
                "Avançar Ano Letivo"
        });
        return ConsoleUI.lerOpcaoMenu("Selecione uma opção");
    }

    public String[] pedirCredenciais() throws OperacaoCanceladaException {
        ConsoleUI.imprimirTitulo("Acesso ao Sistema");
        ConsoleUI.imprimirDicaFormulario();

        // Valida o domínio antes de pedir a password — evita revelar se o email existe.
        String email;
        while (true) {
            email = ConsoleUI.lerString("Email institucional");
            if (email.toLowerCase().endsWith("@issmf.ipp.pt")) break;
            ConsoleUI.imprimirErro("Email inválido. Deve terminar em @issmf.ipp.pt.");
        }

        String password = ConsoleUI.lerPassword("Password");
        return new String[]{email, password};
    }

    /**
     * Sub-menu de consulta pública — não requer login.
     * Mostra: ano letivo atual, lista de cursos, lista de UCs.
     */
    public void mostrarAtalhosConsulta() {
        boolean ativo = true;
        while (ativo) {
            ConsoleUI.imprimirTitulo("Consultar Informação do Sistema");
            ConsoleUI.imprimirMenu(new String[]{
                    "Ano Letivo Atual",
                    "Listar Cursos",
                    "Listar Unidades Curriculares"
            });
            int op = ConsoleUI.lerOpcaoMenu("Opção");
            switch (op) {
                case 1 -> {
                    int ano = RepositorioSessao.getInstance().getAnoAtual();
                    ConsoleUI.imprimirTitulo("Ano Letivo Atual");
                    System.out.printf("  Ano Letivo: %d/%d%n", ano, ano + 1);
                    ConsoleUI.pausar();
                }
                case 2 -> {
                    ConsoleUI.imprimirTitulo("Cursos Disponíveis");
                    List<Curso> cursos = new CursoDAL().listarTodos();
                    if (cursos.isEmpty()) {
                        System.out.println("  Sem cursos registados.");
                    } else {
                        System.out.printf("  %-10s  %-30s  %-12s  %s%n",
                                "SIGLA", "NOME", "DEPARTAMENTO", "PROPINA");
                        ConsoleUI.imprimirLinha();
                        for (Curso c : cursos) {
                            System.out.printf("  %-10s  %-30s  %-12s  %.2f€%n",
                                    c.getSigla(), c.getNome(),
                                    c.getSiglaDepartamento(), c.getPropina());
                        }
                    }
                    ConsoleUI.pausar();
                }
                case 3 -> {
                    ConsoleUI.imprimirTitulo("Unidades Curriculares");
                    List<UC> ucs = new UcDAL().listarTodos();
                    if (ucs.isEmpty()) {
                        System.out.println("  Sem UCs registadas.");
                    } else {
                        System.out.printf("  %-8s  %-25s  %-10s  %s%n",
                                "SIGLA", "NOME", "CURSO", "ANO");
                        ConsoleUI.imprimirLinha();
                        for (UC uc : ucs) {
                            System.out.printf("  %-8s  %-25s  %-10s  %d.º%n",
                                    uc.getSigla(), uc.getNome(),
                                    uc.getSiglaCurso(), uc.getAnoCurricular());
                        }
                    }
                    ConsoleUI.pausar();
                }
                case 0 -> ativo = false;
                default -> mostrarErro("Opção inválida.");
            }
        }
    }

    public String pedirEmailRecuperacao() {
        ConsoleUI.imprimirTitulo("Recuperar Password");
        ConsoleUI.imprimirDicaFormulario();
        return ConsoleUI.lerString("Email da conta a recuperar");
    }

    // =========================================================================
    // FEEDBACK
    // =========================================================================

    public void mostrarSucesso(String msg) {
        ConsoleUI.imprimirSucesso(msg);
        ConsoleUI.pausar();
    }

    public void mostrarErro(String msg) {
        ConsoleUI.imprimirErro(msg);
        ConsoleUI.pausar();
    }

    public void mostrarCancelamento() {
        ConsoleUI.imprimirInfo("Operação cancelada.");
        ConsoleUI.pausar();
    }
}
