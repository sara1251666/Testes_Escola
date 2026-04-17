package common;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utilitário de validações de negócio partilhadas.
 *
 * Idades mínimas (decisão de negócio ISSMF):
 *  - Estudante: 17 anos (mínimo para ingresso no ensino superior).
 *  - Docente  : 21 anos (mínimo razoável para habilitação académica).
 *
 * Formato de data esperado: dd-MM-yyyy  (ex: 25-04-1990).
 */
public final class ValidacaoUtil {

    public static final int IDADE_MINIMA_ESTUDANTE = 17;
    public static final int IDADE_MINIMA_DOCENTE   = 21;

    /** Formato de data partilhado por toda a aplicação (dd-MM-yyyy). */
    public static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private ValidacaoUtil() {}

    // =========================================================================
    // IDADE
    // =========================================================================

    /**
     * Calcula a idade em anos completos a partir de uma data de nascimento.
     *
     * @param dataNascimento Formato dd-MM-yyyy.
     * @return Idade em anos inteiros.
     * @throws DateTimeParseException se o formato for inválido.
     */
    public static int calcularIdade(String dataNascimento) {
        LocalDate nascimento = LocalDate.parse(dataNascimento.trim(), FMT);
        return Period.between(nascimento, LocalDate.now()).getYears();
    }

    /**
     * Verifica se a data de nascimento corresponde a uma idade mínima.
     *
     * @param dataNascimento Formato dd-MM-yyyy.
     * @param idadeMinima    Idade mínima exigida (anos completos).
     * @return true se a idade for suficiente, false caso contrário ou se a data
     *         for inválida / no futuro.
     */
    public static boolean idadeSuficiente(String dataNascimento, int idadeMinima) {
        try {
            LocalDate nascimento = LocalDate.parse(dataNascimento.trim(), FMT);
            if (nascimento.isAfter(LocalDate.now())) return false;
            return calcularIdade(dataNascimento) >= idadeMinima;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Valida o formato dd-MM-yyyy sem verificar intervalo de idade.
     * Útil para validação de formulários antes da verificação de negócio.
     */
    public static boolean formatoDataValido(String data) {
        try {
            LocalDate.parse(data.trim(), FMT);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static String pedirNifValido() {
        while (true) {
            String nif = ConsoleUI.lerString("NIF (9 dígitos)");
            if (nif.matches("\\d{9}")) return nif;
            ConsoleUI.imprimirErro("NIF inválido. Deve ter exatamente 9 dígitos numéricos.");
        }
    }
}
