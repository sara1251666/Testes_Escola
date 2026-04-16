package common;

import java.io.Console;
import java.util.Scanner;

/**
 * Classe utilitária centralizada para toda a interação com a consola.
 *
 * REGRAS DE OURO implementadas aqui:
 *  1. NUNCA usa nextInt(), nextDouble(), etc. — usa sempre nextLine() + parse manual.
 *  2. Qualquer input inválido re-apresenta o prompt (ciclo while-true).
 *  3. Digitar "0" em qualquer campo dispara a mecânica de cancelamento.
 *  4. O método lerPassword() usa System.console() para ocultar a digitação.
 */
public final class ConsoleUI {

    private static final Scanner scanner = new Scanner(System.in);

    // Construtor privado — classe puramente estática
    private ConsoleUI() {}

    /** Limpa o ecrã e imprime um cabeçalho com dupla linha. */
    public static void imprimirCabecalho(String titulo) {
        limparEcra();
        System.out.println("==================================================");
        System.out.printf("  %s%n", titulo.toUpperCase());
        System.out.println("==================================================");
    }

    /** Imprime um sub-título com marcadores. */
    public static void imprimirTitulo(String titulo) {
        System.out.println();
        System.out.println("--- " + titulo + " ---");
    }

    /** Imprime uma linha separadora. */
    public static void imprimirLinha() {
        System.out.println("--------------------------------------------------");
    }

    /**
     * Imprime um menu numerado a partir de um array de strings.
     * A opção 0 ("Voltar / Cancelar") é sempre acrescentada automaticamente.
     */
    public static void imprimirMenu(String[] opcoes) {
        System.out.println();
        for (int i = 0; i < opcoes.length; i++) {
            System.out.printf("  %d - %s%n", i + 1, opcoes[i]);
        }
        System.out.println("  0 - Voltar / Cancelar");
        imprimirLinha();
    }

    /** Imprime uma mensagem de erro formatada. */
    public static void imprimirErro(String mensagem) {
        System.out.println("[ERRO]    " + mensagem);
    }

    /** Imprime uma mensagem de sucesso formatada. */
    public static void imprimirSucesso(String mensagem) {
        System.out.println("[SUCESSO] " + mensagem);
    }

    /** Imprime uma mensagem de informação neutra. */
    public static void imprimirInfo(String mensagem) {
        System.out.println("[INFO]    " + mensagem);
    }

    /** Limpa o terminal (funciona em Unix/Mac; em Windows pode não ter efeito em IDE). */
    public static void limparEcra() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /** Pausa a execução até o utilizador pressionar ENTER. */
    public static void pausar() {
        System.out.println();
        System.out.print("  Pressione [ENTER] para continuar...");
        scanner.nextLine();
    }

    // =========================================================================
    // MÉTODOS DE LEITURA ROBUSTA
    // =========================================================================

    /**
     * Lê uma String não vazia do utilizador.
     * - "0" → pergunta se quer cancelar; se Sim, lança OperacaoCanceladaException.
     *
     * @param prompt Texto a mostrar antes do campo de input.
     * @return A string introduzida (nunca null, nunca vazia).
     * @throws OperacaoCanceladaException se o utilizador confirmar cancelamento.
     */
    public static String lerString(String prompt) {
        while (true) {
            System.out.print("  " + prompt + " (0 para cancelar): ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                imprimirErro("O valor não pode ser vazio. Tente novamente.");
                continue;
            }

            if (input.equals("0")) {
                confirmarCancelamento();
                continue; // Se não confirmou, tenta de novo
            }

            return input;
        }
    }

    /**
     * Lê um inteiro do utilizador de forma segura.
     * - Input não numérico → mostra erro e re-pede.
     * - "0" → mecanismo de cancelamento.
     * Nota: o valor 0 como dado legítimo (ex: opção de menu) é tratado
     *       diretamente pelo lerOpcaoMenu().
     */
    public static int lerInteiro(String prompt) {
        while (true) {
            System.out.print("  " + prompt + " (0 para cancelar): ");
            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                confirmarCancelamento();
                continue;
            }

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                imprimirErro("Número inteiro inválido. Tente novamente.");
            }
        }
    }

    /**
     * Versão especial para ler opções de menu.
     * O valor 0 é devolvido diretamente sem perguntar se quer cancelar
     * (porque "0" nos menus significa "Voltar", não "Cancelar operação").
     */
    public static int lerOpcaoMenu(String prompt) {
        while (true) {
            System.out.print("  " + prompt + ": ");
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                imprimirErro("Opção inválida. Insira um número.");
            }
        }
    }

    /**
     * Lê um double de forma segura.
     * Aceita vírgula ou ponto como separador decimal.
     * Usado para notas e valores monetários.
     */
    public static double lerDouble(String prompt) {
        while (true) {
            System.out.print("  " + prompt + " (0 para cancelar, -1 para falta): ");
            String input = scanner.nextLine().trim().replace(",", ".");

            if (input.equals("0")) {
                confirmarCancelamento();
                continue;
            }

            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                imprimirErro("Número decimal inválido (ex: 14.5). Tente novamente.");
            }
        }
    }

    /**
     * Lê uma nota de avaliação (0–20 ou -1 para falta).
     * Valida o intervalo automaticamente.
     */
    public static double lerNota(String prompt) {
        while (true) {
            double nota = lerDouble(prompt);

            if (nota == -1 || (nota >= 0 && nota <= 20)) {
                return nota;
            }
            imprimirErro("Nota fora do intervalo. Use um valor entre 0 e 20, ou -1 para falta.");
        }
    }

    /**
     * Lê a password com caracteres ocultos (usando System.console()).
     * Se a consola não estiver disponível (ex: IDE), avisa e usa modo visível.
     * "0" → mecanismo de cancelamento.
     */
    public static String lerPassword(String prompt) {
        Console console = System.console();

        while (true) {
            String input;

            if (console != null) {
                char[] chars = console.readPassword("  %s (0 para cancelar): ", prompt);
                if (chars == null) {
                    imprimirErro("Erro ao ler password.");
                    continue;
                }
                input = new String(chars).trim();
                // Apaga os caracteres da memória por segurança
                java.util.Arrays.fill(chars, '\0');
            } else {
                // Modo fallback para IDEs (IntelliJ, VSCode)
                imprimirInfo("Aviso: consola oculta não disponível (modo IDE). Password visível.");
                System.out.print("  " + prompt + " (0 para cancelar): ");
                input = scanner.nextLine().trim();
            }

            if (input.isEmpty()) {
                imprimirErro("A password não pode ser vazia.");
                continue;
            }

            if (input.equals("0")) {
                confirmarCancelamento();
                continue;
            }

            return input;
        }
    }

    /**
     * Lê uma resposta Sim/Não do utilizador.
     * @return true para S/SIM, false para N/NÃO.
     */
    public static boolean lerSimNao(String pergunta) {
        while (true) {
            System.out.print("  " + pergunta + " (S/N): ");
            String input = scanner.nextLine().trim().toUpperCase();

            switch (input) {
                case "S", "SIM" -> { return true; }
                case "N", "NAO", "NÃO" -> { return false; }
                default -> imprimirErro("Responda apenas com 'S' ou 'N'.");
            }
        }
    }

    // =========================================================================
    // MECÂNICA DE CANCELAMENTO (privada)
    // =========================================================================

    /**
     * Pergunta ao utilizador se confirma o cancelamento.
     * Se confirmar, lança OperacaoCanceladaException.
     * Se não confirmar, regressa normalmente (o caller re-apresenta o prompt).
     */
    private static void confirmarCancelamento() {
        System.out.print("  Deseja cancelar a operação atual? (S/N): ");
        String resposta = scanner.nextLine().trim().toUpperCase();

        if (resposta.equals("S") || resposta.equals("SIM")) {
            throw new OperacaoCanceladaException();
        }
        // Se "N" ou qualquer outra coisa, simplesmente regressa e o ciclo re-pede o input
    }
}