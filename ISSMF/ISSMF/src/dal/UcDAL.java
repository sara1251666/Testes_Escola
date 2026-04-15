package dal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UcDAL {
    private static final String FILE_PATH = "data/ucs.csv";
    private static final String DELIMITER = ";";

    /**
     * Valida se um determinado docente é o responsável por uma UC específica.
     * Usado no Lançamento de Notas (DocenteBLL).
     */
    public boolean docenteLecionaUC(int idDocente, String siglaUc) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(DELIMITER);
                if (dados.length >= 5) {
                    String sigla = dados[0];
                    int idResp = Integer.parseInt(dados[4]);

                    if (sigla.equalsIgnoreCase(siglaUc)) {
                        return idResp == idDocente;
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            return false;
        }
        return false;
    }

    /**
     * Retorna a lista de siglas de UCs que pertencem a um docente.
     * Usado para gerar relatórios na DocenteDAL.
     */
    public List<String> obterSiglasPorDocente(int idDocente) {
        List<String> ucs = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(DELIMITER);
                if (dados.length >= 5 && Integer.parseInt(dados[4]) == idDocente) {
                    ucs.add(dados[0].toUpperCase());
                }
            }
        } catch (IOException | NumberFormatException e) {
        }
        return ucs;
    }

    /**
     * Verifica se uma UC existe no ficheiro (Lazy Loading).
     */
    public boolean existeUc(String sigla) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(DELIMITER);
                if (dados.length > 0 && dados[0].equalsIgnoreCase(sigla)) return true;
            }
        } catch (IOException e) { return false; }
        return false;
    }

    /**
     * Adiciona uma nova UC ao ficheiro CSV usando APPEND (O(1)).
     */
    public boolean adicionarUc(String sigla, String nome, String siglaCurso, int ano, int idDocente) {
        String linha = String.join(DELIMITER,
                sigla, nome, siglaCurso, String.valueOf(ano), String.valueOf(idDocente)
        ) + System.lineSeparator();

        try {
            java.nio.file.Files.write(java.nio.file.Paths.get(FILE_PATH), linha.getBytes(),
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) { return false; }
    }
}