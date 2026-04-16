package common;

/**
 * Exceção de runtime lançada quando o utilizador cancela uma operação a meio.
 *
 * MECÂNICA DE CANCELAMENTO:
 *   1. O utilizador digita "0" em qualquer campo da ConsoleUI.
 *   2. A ConsoleUI pergunta: "Deseja cancelar? (S/N)".
 *   3. Se 'S', a ConsoleUI lança esta exceção.
 *   4. O Controller apanha-a no bloco catch e regressa limpo ao menu anterior.
 * Nenhum dado parcial é gravado — o Controller não faz nenhuma chamada à BLL
 * após apanhar esta exceção.
 */
public class OperacaoCanceladaException extends RuntimeException {

    public OperacaoCanceladaException() {
        super("Operação cancelada pelo utilizador.");
    }

    public OperacaoCanceladaException(String contexto) {
        super("Operação cancelada: " + contexto);
    }
}