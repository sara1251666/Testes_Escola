package common;

public class OperacaoCanceladaException extends RuntimeException {
    public OperacaoCanceladaException() {
        super("A operação foi cancelada pelo utilizador.");
    }
}