package view;

import common.ConsoleUI;
import common.OperacaoCanceladaException;
import model.Avaliacao;
import model.Estudante;
import model.Pagamento;

import java.util.List;

/**
 * View do Portal do Estudante.
 * Apenas I/O visual — sem lógica de negócio.
 */
public class EstudanteView {

    // =========================================================================
    // MENUS
    // =========================================================================

    public int mostrarMenuPrincipal() {
        ConsoleUI.imprimirCabecalho("Portal do Estudante");
        ConsoleUI.imprimirMenu(new String[]{
                "Consultar Dados Pessoais",
                "Consultar as Minhas Notas",
                "Atualizar Morada",
                "Alterar Password",
                "Estado Financeiro e Pagamento de Propinas",
                "Transitar para o Próximo Ano"
        });
        return ConsoleUI.lerOpcaoMenu("Selecione uma opção");
    }

    public void mostrarNotas(java.util.List<Avaliacao> notas) {
        ConsoleUI.imprimirTitulo("As Minhas Notas");
        System.out.printf("  %-10s  %-8s  %-8s  %-10s  %-10s%n",
                "UC", "Normal", "Recurso", "Especial", "Situação");
        ConsoleUI.imprimirLinha();
        for (Avaliacao av : notas) {
            System.out.printf("  %-10s  Normal: %-6s  Recurso: %-6s  Especial: %-6s  [%s]%n",
                    av.getSiglaUC(),
                    av.getNotaNormal()   == -1 ? "Falta" : String.format("%.1f", av.getNotaNormal()),
                    av.getNotaRecurso()  == -1 ? "Falta" : String.format("%.1f", av.getNotaRecurso()),
                    av.getNotaEspecial() == -1 ? "Falta" : String.format("%.1f", av.getNotaEspecial()),
                    av.estaAprovado() ? "APROVADO" : "REPROVADO");
        }
        ConsoleUI.pausar();
    }

    // =========================================================================
    // ECRÃS DE CONSULTA
    // =========================================================================

    public void mostrarDadosPessoais(Estudante e) {
        ConsoleUI.imprimirTitulo("Os Seus Dados Pessoais");
        System.out.printf("  Nº Mecanográfico : %d%n", e.getNumMec());
        System.out.printf("  Nome             : %s%n", e.getNome());
        System.out.printf("  NIF              : %s%n", e.getNif());
        System.out.printf("  Email            : %s%n", e.getEmail());
        System.out.printf("  Morada           : %s%n", e.getMorada());
        System.out.printf("  Data Nascimento  : %s%n", e.getDataNascimento());
        System.out.printf("  Curso            : %s%n", e.getSiglaCurso());
        System.out.printf("  Ano Curricular   : %d.º ano%n", e.getAnoCurricular());
        System.out.printf("  Saldo Devedor    : %.2f€%n", e.getSaldoDevedor());
        ConsoleUI.pausar();
    }

    public void mostrarEstadoFinanceiro(Estudante e, List<Pagamento> historico) {
        ConsoleUI.imprimirTitulo("Estado Financeiro");
        System.out.printf("  Saldo Devedor Atual: %.2f€%n%n", e.getSaldoDevedor());

        if (!historico.isEmpty()) {
            System.out.println("  Histórico de Pagamentos (ano atual):");
            double totalPago = 0;
            for (Pagamento p : historico) {
                System.out.println("  " + p);
                totalPago += p.getValor();
            }
            System.out.printf("%n  Total pago este ano: %.2f€%n", totalPago);
        } else {
            System.out.println("  Sem pagamentos registados este ano letivo.");
        }
        ConsoleUI.imprimirLinha();
    }

    // =========================================================================
    // INPUTS
    // =========================================================================

    public String pedirNovaMorada() {
        ConsoleUI.imprimirTitulo("Atualizar Morada");
        ConsoleUI.imprimirDicaFormulario();
        return ConsoleUI.lerString("Nova morada");
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

    public double pedirValorPagamento(double saldoAtual) {
        System.out.printf("  Saldo atual: %.2f€%n", saldoAtual);
        System.out.println("  Opções de pagamento:");
        System.out.println("    1 - Pagar total");
        System.out.println("    2 - Pagar valor parcial");
        System.out.println("    0 - Cancelar");
        ConsoleUI.imprimirLinha();
        int opcao = ConsoleUI.lerOpcaoMenu("Opção");

        return switch (opcao) {
            case 1 -> saldoAtual;
            case 2 -> ConsoleUI.lerDouble("Valor a pagar (€)");
            default -> throw new OperacaoCanceladaException("pagamento cancelado");
        };
    }

    public boolean confirmarTransicaoAno(int anoAtual) {
        return ConsoleUI.lerSimNao(
                "Confirmar transição do " + anoAtual + ".º para o " + (anoAtual + 1) + ".º ano?");
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
        ConsoleUI.imprimirInfo("Operação cancelada. Nenhum dado foi alterado.");
        ConsoleUI.pausar();
    }

    // Auto-matrícula
    public void mostrarCabecalhoAutoMatricula() {
        ConsoleUI.imprimirCabecalho("Auto-Matrícula — ISSMF");
        System.out.println("  Preencha os seus dados para iniciar a matrícula.");
        ConsoleUI.imprimirLinha();
        ConsoleUI.imprimirDicaFormulario();
    }
}
