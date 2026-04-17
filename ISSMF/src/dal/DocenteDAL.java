package dal;

import model.Docente;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * DAL para Docentes.
 *
 * CSV: sigla;email;nome;nif;morada;dataNascimento;ativo
 * Idx:   0     1     2    3    4         5          6
 *
 * O campo 'ativo' (col 6) é opcional para retrocompatibilidade;
 * se ausente assume-se true.
 *
 * O identificador único é a SIGLA (3 letras maiúsculas, ex: "ABC").
 */
public class DocenteDAL {
    private static final String FILE_PATH = "data/docentes.csv";
    private static final String DELIMITER = ";";

    // =========================================================================
    // LEITURA
    // =========================================================================

    /** Obtém docente pela sigla. Devolve null se não existir. */
    public Docente obterPorSigla(String sigla) {
        try (BufferedReader br = abrir()) {
            if (br == null) return null;
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank() || linha.startsWith("sigla")) continue;
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= 6 && d[0].equalsIgnoreCase(sigla)) {
                    return mapear(d);
                }
            }
        } catch (IOException e) { DALUtil.DALUtil.log("DocenteDAL", "DocenteDAL", "obterPorSigla", e); }
        return null;
    }

    /** Verifica se uma sigla já existe. */
    public boolean existeSigla(String sigla) {
        return obterPorSigla(sigla) != null;
    }

    /** Verifica se um NIF já está registado. */
    public boolean existeNif(String nif) {
        try (BufferedReader br = abrir()) {
            if (br == null) return false;
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank() || linha.startsWith("sigla")) continue;
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= 4 && d[3].equals(nif)) return true;
            }
        } catch (IOException e) { DALUtil.DALUtil.log("DocenteDAL", "DocenteDAL", "existeNif", e); }
        return false;
    }

    /** Lista todos os docentes (para ecrã de gestão do gestor). */
    public java.util.List<Docente> listarTodos() {
        java.util.List<Docente> lista = new java.util.ArrayList<>();
        try (BufferedReader br = abrir()) {
            if (br == null) return lista;
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank() || linha.startsWith("sigla")) continue;
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= 6) lista.add(mapear(d));
            }
        } catch (IOException e) { DALUtil.DALUtil.log("DocenteDAL", "DocenteDAL", "listarTodos", e); }
        return lista;
    }

    // =========================================================================
    // ESCRITA
    // =========================================================================

    /** Adiciona um novo docente via APPEND — O(1). */
    public boolean adicionar(Docente docente) {
        new File("data").mkdirs();
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(FILE_PATH, true)))) {
            pw.println(docente.toCSV());
            return true;
        } catch (IOException e) { DALUtil.DALUtil.log("DocenteDAL", "DocenteDAL", "adicionar", e); return false; }
    }

    /**
     * Atualiza um docente existente (ficheiro temp + rename atómico).
     * Usado para alterar o campo 'ativo'.
     */
    public boolean atualizar(Docente docente) {
        File orig = new File(FILE_PATH);
        if (!orig.exists()) return false;
        File temp = new File("data/docentes_temp.csv");
        boolean encontrado = false;

        try (BufferedReader br = new BufferedReader(new FileReader(orig));
             PrintWriter pw = new PrintWriter(new FileWriter(temp))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank()) { pw.println(); continue; }
                if (linha.startsWith("sigla")) { pw.println(linha); continue; }
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= 6 && d[0].equalsIgnoreCase(docente.getSigla())) {
                    pw.println(docente.toCSV());
                    encontrado = true;
                } else {
                    pw.println(linha);
                }
            }
        } catch (IOException e) { temp.delete(); DALUtil.DALUtil.log("DocenteDAL", "DocenteDAL", "atualizar", e); return false; }

        if (encontrado) {
            try {
                Files.move(temp.toPath(), orig.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return true;
            } catch (IOException e) { DALUtil.DALUtil.log("DocenteDAL", "DocenteDAL", "atualizar-move", e); }
        }
        temp.delete();
        return false;
    }

    // =========================================================================
    // PRIVADOS
    // =========================================================================

    /** Retrocompatível: aceita 6 colunas (sem ativo) ou 7 (com ativo). */
    private Docente mapear(String[] d) {
        boolean ativo = d.length >= 7 ? Boolean.parseBoolean(d[6].trim()) : true;
        return new Docente(d[0].trim(), d[1].trim(), d[2].trim(),
                d[3].trim(), d[4].trim(), d[5].trim(), ativo);
    }

    private BufferedReader abrir() {
        File f = new File(FILE_PATH);
        if (!f.exists()) return null;
        try { return new BufferedReader(new FileReader(f)); }
        catch (IOException e) { DALUtil.DALUtil.log("DocenteDAL", "DocenteDAL", "abrir", e); return null; }
    }

}
