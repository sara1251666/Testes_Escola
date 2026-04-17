package dal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Utilitários partilhados por todas as classes DAL.
 *
 * Centraliza as operações repetitivas: parsing seguro de primitivos,
 * substituição atómica de ficheiros e logging de erros de I/O.
 *
 * Antes deste ficheiro, cada DAL tinha as suas próprias cópias privadas
 * de parseInt / parseDouble / mover / log — todos idênticos.
 */
public final class DALUtil {

    private DALUtil() {}

    // =========================================================================
    // PARSING SEGURO
    // =========================================================================

    /** Converte String → int; devolve -1 se o valor for inválido. */
    public static int parseInt(String s) {
        try {
            return Integer.parseInt(s == null ? "" : s.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Converte String → double; devolve 0.0 se o valor for inválido. */
    public static double parseDouble(String s) {
        try {
            return Double.parseDouble(s == null ? "" : s.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // =========================================================================
    // SUBSTITUIÇÃO ATÓMICA DE FICHEIRO
    // =========================================================================

    /**
     * Substitui {@code orig} por {@code temp} de forma atómica.
     *
     * @param sucesso true se a escrita para temp foi bem-sucedida.
     * @param orig    Ficheiro original a substituir.
     * @param temp    Ficheiro temporário com o novo conteúdo.
     * @return true se a substituição foi efetuada com sucesso.
     */
    public static boolean mover(boolean sucesso, File orig, File temp) {
        if (sucesso) {
            try {
                Files.move(temp.toPath(), orig.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return true;
            } catch (IOException e) {
                log("DALUtil", "mover", e);
            }
        }
        temp.delete();
        return false;
    }

    // =========================================================================
    // LOGGING
    // =========================================================================

    /**
     * Regista um erro de I/O no stderr com formato uniforme.
     *
     * @param classe  Nome da classe DAL onde ocorreu o erro (ex: "EstudanteDAL").
     * @param metodo  Nome do método onde ocorreu o erro.
     * @param e       Exceção capturada.
     */
    public static void log(String classe, String metodo, Exception e) {
        System.err.println("[" + classe + "." + metodo + "] " + e.getMessage());
    }
}
