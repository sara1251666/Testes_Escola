package dal;

import model.Curso;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * DAL para Cursos.
 *
 * CSV: sigla;nome;siglaDepartamento;propina;estado
 * Idx:   0     1         2            3       4
 */
public class CursoDAL {
    private static final String FILE_PATH = "data/cursos.csv";
    private static final String DELIMITER = ";";

    /** Verifica se o curso existe pela sigla. */
    public boolean existe(String sigla) {
        return obterPorSigla(sigla) != null;
    }

    /** Obter curso por sigla. Devolve null se não existir. */
    public Curso obterPorSigla(String sigla) {
        try (BufferedReader br = abrir()) {
            if (br == null) return null;
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank() || linha.startsWith("sigla")) continue;
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= 5 && d[0].equalsIgnoreCase(sigla)) {
                    return mapear(d);
                }
            }
        } catch (IOException e) { log("obterPorSigla", e); }
        return null;
    }

    /** Adiciona um novo curso ao ficheiro (APPEND). */
    public boolean adicionar(Curso c) {
        new File("data").mkdirs();
        String linha = c.toCSV() + System.lineSeparator();
        try {
            Files.write(Paths.get(FILE_PATH), linha.getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) { log("adicionar", e); return false; }
    }

    /** Atualiza um curso existente (ficheiro temp + rename atómico). */
    public boolean atualizar(Curso c) {
        File orig = new File(FILE_PATH);
        if (!orig.exists()) return false;
        File temp = new File("data/cursos_temp.csv");
        boolean encontrado = false;

        try (BufferedReader br = new BufferedReader(new FileReader(orig));
             PrintWriter pw = new PrintWriter(new FileWriter(temp))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank()) { pw.println(); continue; }
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= 1 && d[0].equalsIgnoreCase(c.getSigla())) {
                    pw.println(c.toCSV());
                    encontrado = true;
                } else {
                    pw.println(linha);
                }
            }
        } catch (IOException e) { temp.delete(); return false; }

        if (encontrado) {
            try {
                Files.move(temp.toPath(), orig.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return true;
            } catch (IOException e) { log("atualizar", e); }
        }
        temp.delete();
        return false;
    }

    /** Conta o total de cursos (para estatísticas). */
    public int contarTotal() {
        int n = 0;
        try (BufferedReader br = abrir()) {
            if (br == null) return 0;
            String linha;
            while ((linha = br.readLine()) != null) {
                if (!linha.isBlank() && !linha.startsWith("sigla")) n++;
            }
        } catch (IOException e) { log("contarTotal", e); }
        return n;
    }

    // =========================================================================
    // PRIVADOS
    // =========================================================================

    private Curso mapear(String[] d) {
        double propina = 0;
        try { propina = Double.parseDouble(d[3].trim()); } catch (NumberFormatException ignored) {}
        return new Curso(d[0].trim(), d[1].trim(), d[2].trim(), propina, d[4].trim());
    }

    private BufferedReader abrir() {
        File f = new File(FILE_PATH);
        if (!f.exists()) return null;
        try { return new BufferedReader(new FileReader(f)); }
        catch (IOException e) { log("abrir", e); return null; }
    }

    private void log(String m, Exception e) {
        System.err.println("[CursoDAL." + m + "] " + e.getMessage());
    }
}
