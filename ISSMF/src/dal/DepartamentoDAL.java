package dal;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * DAL para Departamentos.
 *
 * CSV: sigla;nome
 * Idx:   0     1
 */
public class DepartamentoDAL {
    private static final String FILE_PATH = "data/departamentos.csv";
    private static final String DELIMITER = ";";

    /** Verifica se um departamento existe pela sua sigla. */
    public boolean existe(String sigla) {
        try (BufferedReader br = abrir()) {
            if (br == null) return false;
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank() || linha.startsWith("sigla")) continue;
                String[] d = linha.split(DELIMITER, -1);
                if (d.length > 0 && d[0].equalsIgnoreCase(sigla)) return true;
            }
        } catch (IOException e) { DALUtil.log("DepartamentoDAL", "existe", e); }
        return false;
    }

    /** Lista todos os departamentos no formato "SIGLA - Nome". */
    public List<String> listarTodos() {
        List<String> lista = new ArrayList<>();
        try (BufferedReader br = abrir()) {
            if (br == null) return lista;
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank() || linha.startsWith("sigla")) continue;
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= 2) lista.add(d[0].trim() + " - " + d[1].trim());
            }
        } catch (IOException e) { DALUtil.log("DepartamentoDAL", "listarTodos", e); }
        return lista;
    }

    /** Adiciona um novo departamento ao ficheiro. */
    public boolean adicionar(String sigla, String nome) {
        new File("data").mkdirs();
        String linha = sigla.toUpperCase() + DELIMITER + nome + System.lineSeparator();
        try {
            Files.write(Paths.get(FILE_PATH), linha.getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) { DALUtil.log("DepartamentoDAL", "adicionar", e); return false; }
    }

    // =========================================================================
    // PRIVADOS
    // =========================================================================

    private BufferedReader abrir() {
        File f = new File(FILE_PATH);
        if (!f.exists()) return null;
        try { return new BufferedReader(new FileReader(f)); }
        catch (IOException e) { DALUtil.log("DepartamentoDAL", "abrir", e); return null; }
    }

}
