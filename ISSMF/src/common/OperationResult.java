package common;

/**
 * Wrapper genérico para o resultado de qualquer operação da BLL.
 * Transporta: sucesso/falha, mensagem e dados opcionais.
 * Padrão de uso no Controller:
 *   OperationResult<Estudante> res = bll.registarAluno(...);
 *   if (res.isSuccess()) { view.mostrarDados(res.getData()); }
 *   else                 { view.mostrarErro(res.getMessage()); }
 */
public final class OperationResult<T> {

    private final boolean success;
    private final String  message;
    private final T       data;

    // --- Construtor privado: usa factories ---
    private OperationResult(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data    = data;
    }

    // --- Factories ---

    /** Cria um resultado de sucesso com dados. */
    public static <T> OperationResult<T> success(String message, T data) {
        return new OperationResult<>(true, message, data);
    }

    /** Cria um resultado de sucesso sem dados. */
    public static <T> OperationResult<T> success(String message) {
        return new OperationResult<>(true, message, null);
    }

    /** Cria um resultado de erro (sem dados). */
    public static <T> OperationResult<T> error(String message) {
        return new OperationResult<>(false, message, null);
    }

    public boolean isSuccess() { return success; }
    public String  getMessage(){ return message;  }
    public T       getData()   { return data;     }

    @Override
    public String toString() {
        return "[" + (success ? "OK" : "ERRO") + "] " + message;
    }
}