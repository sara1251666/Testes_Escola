package view;

import common.ConsoleUI;
import common.OperacaoCanceladaException;

/**
 * View do Painel de Gestão.
 * Apenas I/O visual — sem lógica de negócio.
 */
public class GestorView {

    // =========================================================================
    // MENUS
    // =========================================================================

    public int mostrarMenuPrincipal() {
        ConsoleUI.imprimirCabecalho("Painel de Gestão — ISSMF");
        ConsoleUI.imprimirMenu(new String[]{
                "Registar Novo Estudante",
                "Registar Novo Docente",
                "Gerir Cursos",
                "Gerir Unidades Curriculares",
                "Verificar Quórum para Arranque",
                "Ver Estatísticas Globais",
                "Listar Devedores de Propinas",
                "Alterar Password"
        });
        return ConsoleUI.lerOpcaoMenu("Opção");
    }

    public int mostrarSubMenuCursos() {
        ConsoleUI.imprimirTitulo("Gestão de Cursos");
        ConsoleUI.imprimirMenu(new String[]{
                "Criar Novo Curso",
                "Editar Curso Existente"
        });
        return ConsoleUI.lerOpcaoMenu("Opção");
    }

    // =========================================================================
    // INPUTS — ESTUDANTE
    // =========================================================================

    public String[] pedirDadosEstudante() {
        ConsoleUI.imprimirTitulo("Registar Novo Estudante");
        String nome          = ConsoleUI.lerString("Nome Completo");
        String nif           = pedirNif();
        String email         = ConsoleUI.lerString("Email pessoal (ou 'nao' se não tiver)");
        String dataNasc      = pedirData("Data de Nascimento (dd-MM-yyyy)");
        String morada        = ConsoleUI.lerString("Morada");
        String siglaCurso    = ConsoleUI.lerString("Sigla do Curso").toUpperCase();
        return new String[]{nome, nif, email, dataNasc, morada, siglaCurso};
    }

    // =========================================================================
    // INPUTS — DOCENTE
    // =========================================================================

    public String[] pedirDadosDocente() {
        ConsoleUI.imprimirTitulo("Registar Novo Docente");
        String nome     = ConsoleUI.lerString("Nome Completo");
        String nif      = pedirNif();
        String dataNasc = pedirData("Data de Nascimento (dd-MM-yyyy)");
        String morada   = ConsoleUI.lerString("Morada");
        return new String[]{nome, nif, dataNasc, morada};
    }

    // =========================================================================
    // INPUTS — CURSO
    // =========================================================================

    public String[] pedirDadosCurso() {
        ConsoleUI.imprimirTitulo("Criar Novo Curso");
        String sigla   = ConsoleUI.lerString("Sigla do Curso (ex: LEI)").toUpperCase();
        String nome    = ConsoleUI.lerString("Nome do Curso");
        String dep     = ConsoleUI.lerString("Sigla do Departamento").toUpperCase();
        double propina = ConsoleUI.lerDouble("Propina Anual (€)");
        return new String[]{sigla, nome, dep, String.valueOf(propina)};
    }

    public String[] pedirDadosEdicaoCurso() {
        ConsoleUI.imprimirTitulo("Editar Curso");
        String sigla   = ConsoleUI.lerString("Sigla do Curso a editar").toUpperCase();
        String novoNome= ConsoleUI.lerString("Novo Nome do Curso");
        String novoDep = ConsoleUI.lerString("Nova Sigla do Departamento").toUpperCase();
        double propina = ConsoleUI.lerDouble("Nova Propina Anual (€)");
        return new String[]{sigla, novoNome, novoDep, String.valueOf(propina)};
    }

    // =========================================================================
    // INPUTS — UC
    // =========================================================================

    public String[] pedirDadosUC() {
        ConsoleUI.imprimirTitulo("Criar Nova Unidade Curricular");
        String sigla    = ConsoleUI.lerString("Sigla da UC (ex: P1)").toUpperCase();
        String nome     = ConsoleUI.lerString("Nome da UC");
        String curso    = ConsoleUI.lerString("Sigla do Curso").toUpperCase();
        int    ano      = ConsoleUI.lerInteiro("Ano Curricular (1, 2 ou 3)");
        String docente  = ConsoleUI.lerString("Sigla do Docente Responsável").toUpperCase();
        return new String[]{sigla, nome, curso, String.valueOf(ano), docente};
    }

    // =========================================================================
    // INPUTS — QUÓRUM / PASSWORD
    // =========================================================================

    public String[] pedirDadosQuorum() {
        ConsoleUI.imprimirTitulo("Verificar Quórum");
        String sigla = ConsoleUI.lerString("Sigla do Curso").toUpperCase();
        int    ano   = ConsoleUI.lerInteiro("Ano Curricular (1, 2 ou 3)");
        return new String[]{sigla, String.valueOf(ano)};
    }

    public String pedirNovaPassword() {
        ConsoleUI.imprimirTitulo("Alterar Password");
        String p1 = ConsoleUI.lerPassword("Nova password (mín. 6 caracteres)");
        String p2 = ConsoleUI.lerPassword("Confirmar nova password");
        if (!p1.equals(p2)) {
            mostrarErro("As passwords não coincidem.");
            throw new OperacaoCanceladaException("passwords não coincidem");
        }
        return p1;
    }

    // =========================================================================
    // EXIBIÇÃO
    // =========================================================================

    public void mostrarRelatorio(String titulo, String conteudo) {
        ConsoleUI.imprimirTitulo(titulo);
        System.out.println(conteudo);
        ConsoleUI.pausar();
    }

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

    // =========================================================================
    // PRIVADOS — validação de formato
    // =========================================================================

    private String pedirNif() {
        while (true) {
            String nif = ConsoleUI.lerString("NIF (9 dígitos)");
            if (nif.matches("\\d{9}")) return nif;
            ConsoleUI.imprimirErro("NIF inválido. Deve ter exatamente 9 dígitos numéricos.");
        }
    }

    private String pedirData(String prompt) {
        while (true) {
            String data = ConsoleUI.lerString(prompt);
            if (data.matches("\\d{2}-\\d{2}-\\d{4}")) return data;
            ConsoleUI.imprimirErro("Formato inválido. Use dd-MM-yyyy (ex: 25-04-1990).");
        }
    }
}
