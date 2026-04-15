package common;

import java.io.Console;
import java.util.Scanner;

public class ConsoleUI {
    private static final Scanner scanner = new Scanner(System.in);

    // --- Métodos Visuais ---
    public static void imprimirCabecalho(String msg) {
        limparTela();
        System.out.println("==================================================");
        System.out.println("  " + msg.toUpperCase());
        System.out.println("==================================================");
    }

    public static void imprimirTitulo(String msg) {
        System.out.println("\n--- " + msg + " ---");
    }

    public static void imprimirLinha() {
        System.out.println("--------------------------------------------------");
    }

    public static void imprimirErro(String msg) {
        System.out.println("[ERRO] " + msg);
    }

    public static void imprimirSucesso(String msg) {
        System.out.println("[SUCESSO] " + msg);
    }

    public static void imprimirMenu(String[] opcoes) {
        for (int i = 0; i < opcoes.length; i++) {
            System.out.printf("%d. %s\n", i + 1, opcoes[i]);
        }
        System.out.println("0. Voltar / Cancelar");
        imprimirLinha();
    }

    public static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void pausar() {
        System.out.println("\nPressione [ENTER] para continuar...");
        scanner.nextLine();
    }

    public static String lerString(String prompt) {
        while (true) {
            System.out.print(prompt + " (ou '0' para cancelar): ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                imprimirErro("O valor não pode ser vazio.");
                continue;
            }

            if (input.equals("0")) {
                verificarCancelamento();
                continue;
            }
            return input;
        }
    }

    public static int lerInteiro(String prompt) {
        while (true) {
            String input = lerString(prompt);
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                imprimirErro("Por favor, insira um número inteiro válido.");
            }
        }
    }

    public static String lerPassword(String prompt) {
        Console console = System.console();
        if (console == null) {
            return lerString(prompt + " (Aviso: Consola invisível não suportada aqui)");
        }
        while (true) {
            System.out.print(prompt + " (ou '0' para cancelar): ");
            char[] passwordChars = console.readPassword();
            String input = new String(passwordChars).trim();

            if (input.isEmpty()) {
                imprimirErro("A password não pode ser vazia.");
                continue;
            }
            if (input.equals("0")) {
                verificarCancelamento();
                continue;
            }
            return input;
        }
    }

    private static void verificarCancelamento() {
        System.out.print("Deseja cancelar a operação atual? (S/N): ");
        String conf = scanner.nextLine().trim().toUpperCase();
        if (conf.equals("S") || conf.equals("SIM")) {
            throw new OperacaoCanceladaException();
        }
    }

    public static boolean lerSimNao(String msg) throws OperacaoCanceladaException {
        while (true) {
            System.out.print(msg + " (S/N) (0 para cancelar): ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("0")) {
                verificarCancelamento();
                continue;
            }

            if (input.equals("S") || input.equals("SIM")) return true;
            if (input.equals("N") || input.equals("NAO") || input.equals("NÃO")) return false;

            imprimirErro("Por favor, insira apenas 'S' para Sim ou 'N' para Não.");
        }
    }

    public static double lerDouble(String msg) {
        while (true) {
            System.out.print(msg + " ('C' para cancelar): ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("C") || input.equals("CANCELAR")) {
                System.out.print("Deseja cancelar a operação atual? (S/N): ");
                String confirmacao = scanner.nextLine().trim().toUpperCase();
                if (confirmacao.equals("S") || confirmacao.equals("SIM")) {
                    throw new OperacaoCanceladaException();
                }
                continue;
            }

            try {
                input = input.replace(",", ".");
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                imprimirErro("Número inválido. Use um valor decimal (ex: 14.5 ou 12.0).");
            }
        }
    }
}