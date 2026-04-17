package view;

import common.ConsoleUI;
import common.ValidacaoUtil;
import common.OperacaoCanceladaException;
import java.util.List;

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
                "Gerir Departamentos",
                "Gerir Cursos",
                "Gerir Unidades Curriculares",
                "Verificar Quórum para Arranque",
                "Ver Estatísticas Globais",
                "Listar Devedores de Propinas",
                "Gerir Estado de Utilizadores (Ativar/Desativar)",
                "Alterar Password"
        });
        return ConsoleUI.lerOpcaoMenu("Opção");
    }

    public int mostrarSubMenuDepartamentos() {
        ConsoleUI.imprimirTitulo("Gestão de Departamentos");
        ConsoleUI.imprimirMenu(new String[]{
                "Registar Novo Departamento",
                "Listar Departamentos"
        });
        return ConsoleUI.lerOpcaoMenu("Opção");
    }

    public String[] pedirDadosDepartamento() {
        ConsoleUI.imprimirTitulo("Registar Novo Departamento");
        ConsoleUI.imprimirDicaFormulario();
        String sigla = ConsoleUI.lerString("Sigla do Departamento (ex: DEI)").toUpperCase();
        String nome  = ConsoleUI.lerString("Nome do Departamento");
        return new String[]{sigla, nome};
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
        ConsoleUI.imprimirDicaFormulario();
        String nome          = ConsoleUI.lerString("Nome Completo");
        String nif           = ValidacaoUtil.pedirNifValido();
        String email         = ConsoleUI.lerString("Email pessoal (ou 'nao' se não tiver)");
        String dataNasc      = ConsoleUI.lerData("Data de Nascimento (dd-MM-yyyy)");
        String morada        = ConsoleUI.lerString("Morada");
        String siglaCurso    = ConsoleUI.lerString("Sigla do Curso").toUpperCase();
        return new String[]{nome, nif, email, dataNasc, morada, siglaCurso};
    }

    // =========================================================================
    // INPUTS — DOCENTE
    // =========================================================================

    public String[] pedirDadosDocente() {
        ConsoleUI.imprimirTitulo("Registar Novo Docente");
        ConsoleUI.imprimirDicaFormulario();
        String nome     = ConsoleUI.lerString("Nome Completo");
        String nif      = ValidacaoUtil.pedirNifValido();
        String dataNasc = ConsoleUI.lerData("Data de Nascimento (dd-MM-yyyy)");
        String morada   = ConsoleUI.lerString("Morada");
        return new String[]{nome, nif, dataNasc, morada};
    }

    // =========================================================================
    // INPUTS — CURSO
    // =========================================================================

    public String[] pedirDadosCurso() {
        ConsoleUI.imprimirTitulo("Criar Novo Curso");
        ConsoleUI.imprimirDicaFormulario();
        String sigla   = ConsoleUI.lerString("Sigla do Curso (ex: LEI)").toUpperCase();
        String nome    = ConsoleUI.lerString("Nome do Curso");
        String dep     = ConsoleUI.lerString("Sigla do Departamento").toUpperCase();
        double propina = ConsoleUI.lerDouble("Propina Anual (€)");
        return new String[]{sigla, nome, dep, String.valueOf(propina)};
    }

    public String[] pedirDadosEdicaoCurso() {
        ConsoleUI.imprimirTitulo("Editar Curso");
        ConsoleUI.imprimirDicaFormulario();
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
        ConsoleUI.imprimirDicaFormulario();
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
        ConsoleUI.imprimirDicaFormulario();
        String sigla = ConsoleUI.lerString("Sigla do Curso").toUpperCase();
        int    ano   = ConsoleUI.lerInteiro("Ano Curricular (1, 2 ou 3)");
        return new String[]{sigla, String.valueOf(ano)};
    }

    // =========================================================================
    // SUB-MENU — ESTADO DE UTILIZADORES
    // =========================================================================

    public int mostrarSubMenuEstadoUtilizadores() {
        ConsoleUI.imprimirTitulo("Gerir Estado de Utilizadores");
        ConsoleUI.imprimirMenu(new String[]{
                "Listar e Gerir Estudantes (Ativar/Desativar)",
                "Listar e Gerir Docentes (Ativar/Desativar)"
        });
        return ConsoleUI.lerOpcaoMenu("Opção");
    }

    public int pedirNumMecEstudante() {
        return ConsoleUI.lerInteiro("Número Mecanográfico do Estudante");
    }

    public String pedirSiglaDocente() {
        return ConsoleUI.lerString("Sigla do Docente (ex: ABC)").toUpperCase();
    }

    public boolean pedirNovoEstado(String nomeEntidade) {
        ConsoleUI.imprimirTitulo("Alterar Estado — " + nomeEntidade);
        System.out.println("  1 - Ativar");
        System.out.println("  2 - Desativar");
        ConsoleUI.imprimirLinha();
        int opcao = ConsoleUI.lerOpcaoMenu("Opção");
        return opcao == 1; // true = ativar, false = desativar
    }

    public String pedirNovaPassword() {
        ConsoleUI.imprimirTitulo("Alterar Password");
        ConsoleUI.imprimirDicaFormulario();
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

}
