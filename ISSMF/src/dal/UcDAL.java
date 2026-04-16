package dal;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * DAL para Unidades Curriculares.
 *
 * CSV: sigla;nome;anoCurricular;siglaDocente;siglaCurso
 * Idx:   0     1       2             3           4
 *
 * NOTA: o identificador do docente é a sua SIGLA (String, ex: "ABC"),
 * nunca um inteiro.
 */
public class UcDAL {
    private static final String FILE_PATH = "data/ucs.csv";
    private static final String DELIMITER = ";";

    // =========================================================================
    // PESQUISA
    // =========================================================================

    /**
     * Verifica se uma UC existe globalmente (qualquer curso).
     * Usado para informação geral.
     */
    public boolean existe(String sigla) {
        try (BufferedReader br = abrir()) {
            if (br == null) return false;
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank() || linha.startsWith("sigla")) continue;
                String[] d = linha.split(DELIMITER, -1);
                if (d.length > 0 && d[0].equalsIgnoreCase(sigla)) return true;
            }
        } catch (IOException e) { log("existe", e); }
        return false;
    }

    /**
     * Verifica se uma UC com a mesma sigla já existe NESTE curso específico.
     * Permite que a mesma UC (mesma sigla) esteja associada a vários cursos,
     * mas impede duplicados dentro do mesmo curso.
     * (Requisito enunciado v1.0: "Uma UC pode estar registada em vários cursos.")
     */
    public boolean existeNoCurso(String siglaUc, String siglaCurso) {
        try (BufferedReader br = abrir()) {
            if (br == null) return false;
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank() || linha.startsWith("sigla")) continue;
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= 5
                        && d[0].equalsIgnoreCase(siglaUc)
                        && d[4].equalsIgnoreCase(siglaCurso)) {
                    return true;
                }
            }
        } catch (IOException e) { log("existeNoCurso", e); }
        return false;
    }

    /**
     * Verifica se um docente (por sigla) é responsável por uma UC.
     * Usado pelo DocenteController para validar lançamento de notas.
     */
    public boolean docenteLecionaUC(String siglaDocente, String siglaUc) {
        try (BufferedReader br = abrir()) {
            if (br == null) return false;
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank() || linha.startsWith("sigla")) continue;
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= 5
                        && d[0].equalsIgnoreCase(siglaUc)
                        && d[3].equalsIgnoreCase(siglaDocente)) {
                    return true;
                }
            }
        } catch (IOException e) { log("docenteLecionaUC", e); }
        return false;
    }

    /**
     * Devolve as siglas de todas as UCs lecionadas por um docente (por sigla).
     */
    public List<String> obterSiglasPorDocente(String siglaDocente) {
        List<String> lista = new ArrayList<>();
        try (BufferedReader br = abrir()) {
            if (br == null) return lista;
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank() || linha.startsWith("sigla")) continue;
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= 4 && d[3].equalsIgnoreCase(siglaDocente)) {
                    lista.add(d[0].toUpperCase());
                }
            }
        } catch (IOException e) { log("obterSiglasPorDocente", e); }
        return lista;
    }

    /**
     * Devolve as siglas das UCs de um curso num determinado ano curricular.
     * Usado na transição de ano do estudante para verificar aproveitamento.
     */
    public List<String> obterSiglasPorCursoEAno(String siglaCurso, int anoCurricular) {
        List<String> lista = new ArrayList<>();
        try (BufferedReader br = abrir()) {
            if (br == null) return lista;
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank() || linha.startsWith("sigla")) continue;
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= 5
                        && d[4].equalsIgnoreCase(siglaCurso)
                        && parseInt(d[2]) == anoCurricular) {
                    lista.add(d[0].toUpperCase());
                }
            }
        } catch (IOException e) { log("obterSiglasPorCursoEAno", e); }
        return lista;
    }

    /**
     * Conta quantas UCs um docente já leciona (limite máximo imposto pelo gestor).
     */
    public int contarPorDocente(String siglaDocente) {
        int n = 0;
        try (BufferedReader br = abrir()) {
            if (br == null) return 0;
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank() || linha.startsWith("sigla")) continue;
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= 4 && d[3].equalsIgnoreCase(siglaDocente)) n++;
            }
        } catch (IOException e) { log("contarPorDocente", e); }
        return n;
    }

    /**
     * Conta quantas UCs existem num curso num dado ano curricular.
     * Usado para verificar o limite de 5 UCs/ano/curso.
     */
    public int contarPorCursoEAno(String siglaCurso, int ano) {
        int n = 0;
        try (BufferedReader br = abrir()) {
            if (br == null) return 0;
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank() || linha.startsWith("sigla")) continue;
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= 5
                        && d[4].equalsIgnoreCase(siglaCurso)
                        && parseInt(d[2]) == ano) {
                    n++;
                }
            }
        } catch (IOException e) { log("contarPorCursoEAno", e); }
        return n;
    }

    // =========================================================================
    // ESCRITA
    // =========================================================================

    /**
     * Adiciona uma nova UC ao ficheiro CSV.
     * Formato: sigla;nome;anoCurricular;siglaDocente;siglaCurso
     */
    public boolean adicionar(String sigla, String nome, String siglaCurso,
                              int anoCurricular, String siglaDocente) {
        new File("data").mkdirs();
        String linha = String.join(DELIMITER,
                sigla.toUpperCase(), nome,
                String.valueOf(anoCurricular),
                siglaDocente.toUpperCase(),
                siglaCurso.toUpperCase()
        ) + System.lineSeparator();

        try {
            Files.write(Paths.get(FILE_PATH), linha.getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) { log("adicionar", e); return false; }
    }

    // =========================================================================
    // PRIVADOS
    // =========================================================================

    private BufferedReader abrir() {
        File f = new File(FILE_PATH);
        if (!f.exists()) return null;
        try { return new BufferedReader(new FileReader(f)); }
        catch (IOException e) { log("abrir", e); return null; }
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return -1; }
    }

    private void log(String m, Exception e) {
        System.err.println("[UcDAL." + m + "] " + e.getMessage());
    }
}
