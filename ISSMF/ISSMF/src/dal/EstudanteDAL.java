package dal;

import model.Estudante;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * DAL para Estudantes.
 *
 * CSV: numMec;email;nome;nif;morada;dataNascimento;anoInscricao;siglaCurso;saldoDevedor;anoCurricular
 * Idx:   0      1     2    3    4         5              6          7           8             9
 *
 * LAZY LOADING: todas as pesquisas lêem linha a linha e abortam ao primeiro match.
 * A lista completa (listarDevedores) é a única excepção justificada pelo requisito.
 */
public class EstudanteDAL {

    private static final String FILE_PATH = "data/estudantes.csv";
    private static final String DELIMITER = ";";
    private static final int    MIN_COLS  = 10;

    // Índices das colunas
    private static final int C_NUM_MEC   = 0;
    private static final int C_EMAIL     = 1;
    private static final int C_NOME      = 2;
    private static final int C_NIF       = 3;
    private static final int C_MORADA    = 4;
    private static final int C_DATA_NASC = 5;
    private static final int C_ANO_INS   = 6;
    private static final int C_CURSO     = 7;
    private static final int C_SALDO     = 8;
    private static final int C_ANO_CURR  = 9;

    // =========================================================================
    // LEITURA (Lazy Loading)
    // =========================================================================

    /** Procura por numMec. Lazy: aborta ao primeiro match. */
    public Estudante obterPorId(int numMec) {
        try (BufferedReader br = abrir()) {
            if (br == null) return null;
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= MIN_COLS && parseInt(d[C_NUM_MEC]) == numMec)
                    return mapear(d);  // << abortamos aqui
            }
        } catch (IOException e) { log("obterPorId", e); }
        return null;
    }

    /** Procura por email. Lazy: aborta ao primeiro match. */
    public Estudante obterPorEmail(String email) {
        try (BufferedReader br = abrir()) {
            if (br == null) return null;
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= MIN_COLS && d[C_EMAIL].equalsIgnoreCase(email))
                    return mapear(d);
            }
        } catch (IOException e) { log("obterPorEmail", e); }
        return null;
    }

    /** Verifica existência por numMec. Lazy. */
    public boolean existePorId(int numMec) {
        return obterPorId(numMec) != null;
    }

    /** Verifica se o NIF já está registado. Lazy: aborta ao primeiro match. */
    public boolean existeNif(String nif) {
        try (BufferedReader br = abrir()) {
            if (br == null) return false;
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= MIN_COLS && d[C_NIF].equals(nif)) return true;
            }
        } catch (IOException e) { log("existeNif", e); }
        return false;
    }

    /** Verifica se o email já está registado. Lazy: aborta ao primeiro match. */
    public boolean existeEmail(String email) {
        try (BufferedReader br = abrir()) {
            if (br == null) return false;
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= MIN_COLS && d[C_EMAIL].equalsIgnoreCase(email)) return true;
            }
        } catch (IOException e) { log("existeEmail", e); }
        return false;
    }

    /** Conta alunos num curso (para verificar quórum de arranque). */
    public int contarPorCurso(String siglaCurso) {
        int n = 0;
        try (BufferedReader br = abrir()) {
            if (br == null) return 0;
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= MIN_COLS && d[C_CURSO].equalsIgnoreCase(siglaCurso)) n++;
            }
        } catch (IOException e) { log("contarPorCurso", e); }
        return n;
    }

    /** Conta total de estudantes. */
    public int contarTotal() {
        int n = 0;
        try (BufferedReader br = abrir()) {
            if (br == null) return 0;
            while (br.readLine() != null) n++;
        } catch (IOException e) { log("contarTotal", e); }
        return n;
    }

    /**
     * Lista estudantes com saldo > 0 (relatório de devedores do gestor).
     * Necessário carregar múltiplos — requisito justifica.
     */
    public List<Estudante> listarDevedores() {
        List<Estudante> lista = new ArrayList<>();
        try (BufferedReader br = abrir()) {
            if (br == null) return lista;
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= MIN_COLS && parseDouble(d[C_SALDO]) > 0)
                    lista.add(mapear(d));
            }
        } catch (IOException e) { log("listarDevedores", e); }
        return lista;
    }

    /**
     * Lista todos os estudantes de um determinado ano curricular + curso.
     * Usado na transição de ano para verificar aproveitamento.
     */
    public List<Estudante> listarPorCursoEAno(String siglaCurso, int anoCurricular) {
        List<Estudante> lista = new ArrayList<>();
        try (BufferedReader br = abrir()) {
            if (br == null) return lista;
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= MIN_COLS
                        && d[C_CURSO].equalsIgnoreCase(siglaCurso)
                        && parseInt(d[C_ANO_CURR]) == anoCurricular)
                    lista.add(mapear(d));
            }
        } catch (IOException e) { log("listarPorCursoEAno", e); }
        return lista;
    }

    // =========================================================================
    // ESCRITA
    // =========================================================================

    /** Adiciona novo estudante via APPEND — O(1). */
    public boolean adicionar(Estudante e) {
        preparar();
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(FILE_PATH, true)))) {
            pw.println(e.toCSV());
            return true;
        } catch (IOException ex) { log("adicionar", ex); return false; }
    }

    /**
     * Atualiza um estudante existente.
     * Ficheiro temp + rename atómico — sem carregar tudo em memória.
     */
    public boolean atualizar(Estudante e) {
        File orig = new File(FILE_PATH);
        if (!orig.exists()) return false;
        File temp  = new File("data/estudantes_temp.csv");
        boolean ok = false;

        try (BufferedReader br = new BufferedReader(new FileReader(orig));
             PrintWriter    pw = new PrintWriter(new FileWriter(temp))) {

            String linha;
            while ((linha = br.readLine()) != null) {
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= MIN_COLS && parseInt(d[C_NUM_MEC]) == e.getNumMec()) {
                    pw.println(e.toCSV());
                    ok = true;
                } else {
                    pw.println(linha);
                }
            }
        } catch (IOException ex) { temp.delete(); log("atualizar", ex); return false; }

        return mover(ok, orig, temp);
    }

    // =========================================================================
    // UTILITÁRIOS
    // =========================================================================

    /**
     * Gera o próximo numMec disponível (ano * 10000 + sequência).
     * Lê o ficheiro uma vez para encontrar o máximo atual.
     */
    public int gerarProximoNumMec(int anoAtual) {
        int max = anoAtual * 10000;
        try (BufferedReader br = abrir()) {
            if (br == null) return max + 1;
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= 1) {
                    int n = parseInt(d[0]);
                    if (n > max) max = n;
                }
            }
        } catch (IOException e) { log("gerarProximoNumMec", e); }
        return max + 1;
    }

    // =========================================================================
    // PRIVADOS
    // =========================================================================

    private Estudante mapear(String[] d) {
        return new Estudante(
                parseInt(d[C_NUM_MEC]),
                d[C_EMAIL].trim(),
                d[C_NOME].trim(),
                d[C_NIF].trim(),
                d[C_MORADA].trim(),
                d[C_DATA_NASC].trim(),
                parseInt(d[C_ANO_INS]),
                d[C_CURSO].trim(),
                parseDouble(d[C_SALDO]),
                parseInt(d[C_ANO_CURR])
        );
    }

    private BufferedReader abrir() {
        File f = new File(FILE_PATH);
        if (!f.exists()) return null;
        try { return new BufferedReader(new FileReader(f)); }
        catch (IOException e) { log("abrir", e); return null; }
    }

    private void preparar() {
        new File("data").mkdirs();
    }

    private boolean mover(boolean sucesso, File orig, File temp) {
        if (sucesso) {
            try {
                Files.move(temp.toPath(), orig.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return true;
            } catch (IOException e) { log("mover", e); }
        }
        temp.delete();
        return false;
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return -1; }
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s.trim()); } catch (NumberFormatException e) { return 0.0; }
    }

    private void log(String metodo, Exception e) {
        System.err.println("[EstudanteDAL." + metodo + "] " + e.getMessage());
    }
}
