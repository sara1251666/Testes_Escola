package common;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import java.io.Console;
import java.util.Scanner;

/**
 * Classe utilitária centralizada para toda a interação com a consola.
 *
 * REGRAS DE OURO implementadas aqui:
 *  1. NUNCA usa nextInt(), nextDouble(), etc. — usa sempre nextLine() + parse manual.
 *  2. Qualquer input inválido re-apresenta o prompt (ciclo while-true).
 *  3. "sair"  em qualquer campo → pede confirmação antes de cancelar.
 *     "voltar" em qualquer campo → cancela imediatamente, sem confirmação.
 *     O valor "0" é agora um valor legítimo nos formulários.
 *  4. Em menus, o valor "0" continua a significar "Voltar / Cancelar" (sem confirmação).
 *  5. O método lerPassword() usa System.console() para ocultar a digitação.
 *  6. A dica de cancelamento é mostrada UMA VEZ, logo abaixo do título do
 *     formulário, via imprimirDicaFormulario() — não repetida em cada campo.
 */
public final class ConsoleUI {

    private static final Scanner scanner = new Scanner(System.in);

    private static Terminal terminal;
    private static LineReader lineReader;

    static {
        try {
            terminal = TerminalBuilder.builder()
                    .system(true)
                    .build();
            lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();
        } catch (Exception e) {
            System.err.println("[Aviso] Não foi possível inicializar o terminal JLine: " + e.getMessage());
        }
    }

    private ConsoleUI() {}

    /** Limpa o ecrã e imprime um cabeçalho com dupla linha. */
    public static void imprimirCabecalho(String titulo) {
        limparEcra();
        int larguraTotal = 50;
        String tituloFormatado = " " + titulo.toUpperCase() + " ";
        int tamanhoTexto = tituloFormatado.length();

        int espacosLado = (larguraTotal - 2 - tamanhoTexto) / 2;

        System.out.print("╔");
        for (int i = 0; i < larguraTotal - 2; i++) System.out.print("═");
        System.out.println("╗");

        System.out.print("║");
        for (int i = 0; i < espacosLado; i++) System.out.print(" ");
        System.out.print(tituloFormatado);
        int resto = (larguraTotal - 2 - tamanhoTexto) % 2;
        for (int i = 0; i < espacosLado + resto; i++) System.out.print(" ");
        System.out.println("║");

        System.out.print("╚");
        for (int i = 0; i < larguraTotal - 2; i++) System.out.print("═");
        System.out.println("╝");
    }

    /**
     * Imprime um sub-título com uma moldura simples, mantendo o estilo Retro.
     * Ideal para separar secções dentro da mesma página.
     */
    public static void imprimirTitulo(String titulo) {
        int larguraTotal = 40;
        String texto = " " + titulo + " ";
        int tamanhoTexto = texto.length();

        int espacosLado = (larguraTotal - 2 - tamanhoTexto) / 2;
        if (espacosLado < 0) espacosLado = 0;

        System.out.println();

        System.out.print("  ┌");
        for (int i = 0; i < larguraTotal - 2; i++) System.out.print("─");
        System.out.println("┐");

        System.out.print("  │");
        for (int i = 0; i < espacosLado; i++) System.out.print(" ");
        System.out.print(texto);

        int resto = (larguraTotal - 2 - tamanhoTexto) % 2;
        if (resto < 0) resto = 0;
        for (int i = 0; i < espacosLado + resto; i++) System.out.print(" ");
        System.out.println("│");

        // Linha inferior (└───┘)
        System.out.print("  └");
        for (int i = 0; i < larguraTotal - 2; i++) System.out.print("─");
        System.out.println("┘");
    }

    public static void imprimirLinha() {
        int larguraTotal = 40;
        System.out.print("  ");
        for (int i = 0; i < larguraTotal; i++) {
            System.out.print("─");
        }
        System.out.println();
    }
    /**
     * Imprime um menu numerado a partir de um array de strings.
     * A opção 0 ("Voltar / Cancelar") é sempre acrescentada automaticamente.
     */
    public static void imprimirMenu(String[] opcoes) {
        int larguraTotal = 50;
        System.out.println();

        System.out.print("┌");
        for (int i = 0; i < larguraTotal - 2; i++) System.out.print("─");
        System.out.println("┐");

        for (int i = 0; i < opcoes.length; i++) {
            String textoOpcao = "[" + (i + 1) + "] " + opcoes[i];
            System.out.print("│ ");
            System.out.print(textoOpcao);

           int espacosFaltam = larguraTotal - 3 - textoOpcao.length();

            for (int j = 0; j < espacosFaltam; j++) System.out.print(" ");
            System.out.println("│");
        }

        System.out.print("├");
        for (int i = 0; i < larguraTotal - 2; i++) System.out.print("─");
        System.out.println("┤");

        String textoSair = "[0] VOLTAR / CANCELAR";
        System.out.print("│ ");
        System.out.print(textoSair);
        int espacosFaltamSair = larguraTotal - 3 - textoSair.length();
        for (int j = 0; j < espacosFaltamSair; j++) System.out.print(" ");
        System.out.println("│");

        System.out.print("└");
        for (int i = 0; i < larguraTotal - 2; i++) System.out.print("─");
        System.out.println("┘");

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

    /**
     * Imprime a dica de cancelamento UMA VEZ, logo abaixo do título de um formulário.
     * Deve ser chamado após imprimirTitulo() ou imprimirCabecalho() em ecrãs de input.
     */
    public static void imprimirDicaFormulario() {
        System.out.println("  ('sair' para cancelar | 'voltar' para retroceder imediatamente)");
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
     * - "sair"   → pede confirmação antes de cancelar.
     * - "voltar" → cancela imediatamente, sem confirmação.
     *
     * @param prompt Texto a mostrar antes do campo de input.
     * @return A string introduzida (nunca null, nunca vazia).
     * @throws OperacaoCanceladaException se o utilizador cancelar.
     */
    public static String lerString(String prompt) {
        while (true) {
            System.out.print("  " + prompt + ": ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                imprimirErro("O valor não pode ser vazio. Tente novamente.");
                continue;
            }
            if (input.equalsIgnoreCase("sair")) {
                confirmarCancelamento();
                continue;
            }

            return input;
        }
    }

    /**
     * Lê um inteiro do utilizador de forma segura.
     * - Input não numérico → mostra erro e re-pede.
     * - "sair"   → pede confirmação antes de cancelar.
     * - "voltar" → cancela imediatamente, sem confirmação.
     * O valor 0 é agora um inteiro legítimo; para menus usa-se lerOpcaoMenu().
     */
    public static int lerInteiro(String prompt) {
        while (true) {
            System.out.print("  " + prompt + ": ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("sair")) {
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
     * - "sair"   → pede confirmação antes de cancelar.
     * - "voltar" → cancela imediatamente, sem confirmação.
     * Usado para valores monetários (propina, pagamento).
     */
    public static double lerDouble(String prompt) {
        while (true) {
            System.out.print("  " + prompt + ": ");
            String input = scanner.nextLine().trim().replace(",", ".");

            if (input.equalsIgnoreCase("sair")) {
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
     * - "sair"   → pede confirmação antes de cancelar.
     * - "voltar" → cancela imediatamente, sem confirmação.
     * -1 significa "falta". Intervalo 0–20 validado.
     */
    public static double lerNota(String prompt) {
        while (true) {
            System.out.print("  " + prompt + " (-1 para falta): ");
            String input = scanner.nextLine().trim().replace(",", ".");

            if (input.equalsIgnoreCase("sair")) {
                confirmarCancelamento();
                continue;
            }

            try {
                double nota = Double.parseDouble(input);
                if (nota == -1 || (nota >= 0 && nota <= 20)) return nota;
                imprimirErro("Nota fora do intervalo. Use 0–20 ou -1 para falta.");
            } catch (NumberFormatException e) {
                imprimirErro("Valor inválido. Exemplo: 14.5 ou -1 para falta.");
            }
        }
    }

    /**
     * Lê a password de forma segura com mascaramento de caracteres.
     *
     * Ordem de tentativas:
     *  1. JLine (funciona no IntelliJ e em terminais reais) — mostra '****' enquanto digita.
     *  2. System.console() (funciona em terminais reais fora de IDE) — digitação invisível.
     *  3. Scanner simples (fallback de último recurso) — password visível, com aviso.
     *
     * - "sair"   → pede confirmação antes de cancelar.
     * - "voltar" → cancela imediatamente, sem confirmação.
     */
    public static String lerPassword(String prompt) {
        while (true) {
            String input;

            if (lineReader != null) {
                // JLine: mascara a digitação com '*'; funciona no terminal do IntelliJ
                try {
                    input = lineReader.readLine("  " + prompt + ": ", '*');
                    if (input == null) { imprimirErro("Erro ao ler password."); continue; }
                    input = input.trim();
                } catch (Exception e) {
                    imprimirErro("Erro JLine: " + e.getMessage());
                    continue;
                }
            } else {
                Console console = System.console();
                if (console != null) {
                    char[] chars = console.readPassword("  %s: ", prompt);
                    if (chars == null) { imprimirErro("Erro ao ler password."); continue; }
                    input = new String(chars).trim();
                    java.util.Arrays.fill(chars, '\0');
                } else {
                    imprimirInfo("Modo de fallback: a password ficará visível.");
                    System.out.print("  " + prompt + ": ");
                    input = scanner.nextLine().trim();
                }
            }

            if (input.isEmpty()) {
                imprimirErro("A password não pode ser vazia.");
                continue;
            }
            if (input.equalsIgnoreCase("voltar")) {
                throw new OperacaoCanceladaException("voltar");
            }
            if (input.equalsIgnoreCase("sair")) {
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

    /**
     * Pergunta ao utilizador se confirma o cancelamento.
     * Se confirmar, lança OperacaoCanceladaException.
     * Se não confirmar, regressa normalmente (o caller re-apresenta o prompt).
     * Também aceita "voltar" para regressar sem cancelar (útil quando o utilizador
     * se enganou na escrita e quer simplesmente re-escrever o campo).
     */
    private static void confirmarCancelamento() {
        System.out.println();
        System.out.println("  Opções:");
        System.out.println("    [S] / [SIM]    — Cancelar a operação e voltar ao menu");
        System.out.println("    [N] / [NÃO]    — Continuar a preencher o formulário");
        System.out.print("  Deseja cancelar a operação atual? (S/N): ");
        String resposta = scanner.nextLine().trim().toUpperCase();

        if (resposta.equals("S") || resposta.equals("SIM")) {
            throw new OperacaoCanceladaException();
        }
        imprimirInfo("A continuar o preenchimento...");
    }

    /**
     * Lê uma data no formato dd-MM-yyyy.
     * Valida apenas o formato — a validação de negócio (ex: idade mínima) fica na BLL.
     * - "sair"   → pede confirmação antes de cancelar.
     * - "voltar" → cancela imediatamente, sem confirmação.
     */
    public static String lerData(String prompt) {
        while (true) {
            String data = lerString(prompt);
            if (ValidacaoUtil.formatoDataValido(data)) return data;
            imprimirErro("Formato inválido. Use dd-MM-yyyy (ex: 25-04-1990).");
        }
    }

    /**
     * Centraliza a lógica de: Pergunta (S/N) -> Listagem opcional -> Leitura de Input.
     * Aproveita os métodos de formatação Retro da classe.
     */
    public static String lerInputComOpcaoListagem(String nomeCampo, Runnable acaoListar) throws OperacaoCanceladaException {
        while (true) {
            imprimirTitulo("Seleção de " + nomeCampo);
            boolean desejaVer = lerSimNao("Deseja ver a lista de " + nomeCampo + " disponíveis?");
            if (desejaVer) {
                System.out.println("\n  A carregar dados...");
                acaoListar.run();
                imprimirLinha();
            }
            return lerString("Digite a sigla/código de " + nomeCampo);
        }
    }
}