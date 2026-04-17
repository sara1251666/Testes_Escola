package dal;

import model.Pagamento;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * DAL para o histórico de pagamentos de propinas.
 *
 * CSV: numMec;anoLetivo;valor;data
 * Idx:   0       1        2    3
 *
 * Cada linha representa UM pagamento (pode haver vários por aluno/ano — pagamentos parciais).
 */
public class PagamentoDAL {

    private static final String FILE_PATH = "data/pagamentos.csv";
    private static final String DELIMITER = ";";

    // =========================================================================
    // LEITURA (Lazy Loading)
    // =========================================================================

    /**
     * Lista todos os pagamentos de um estudante num determinado ano letivo.
     * Necessário para o ecrã de histórico — justifica carregar múltiplas linhas.
     */
    public List<Pagamento> listarPorAlunoEAno(int numMec, int anoLetivo) {
        List<Pagamento> lista = new ArrayList<>();
        File f = new File(FILE_PATH);
        if (!f.exists()) return lista;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= 4
                        && DALUtil.parseInt(d[0]) == numMec
                        && DALUtil.parseInt(d[1]) == anoLetivo)
                    lista.add(new Pagamento(numMec, anoLetivo,
                            DALUtil.parseDouble(d[2]), d[3].trim()));
            }
        } catch (IOException e) { DALUtil.log("PagamentoDAL", "listarPorAlunoEAno", e); }
        return lista;
    }

    // =========================================================================
    // ESCRITA
    // =========================================================================

    /**
     * Regista um pagamento via APPEND — O(1).
     */
    public boolean registar(Pagamento p) {
        new File("data").mkdirs();
        String linha = p.toCSV() + System.lineSeparator();
        try {
            Files.write(Paths.get(FILE_PATH), linha.getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) { DALUtil.log("PagamentoDAL", "registar", e); return false; }
    }

    // =========================================================================
    // PRIVADOS
    // =========================================================================



}
