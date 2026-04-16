package dal;

import model.UnidadeCurricular;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class UcDAL {
    private static final String FILE_PATH = "data/ucs.csv";
    private static final String DELIMITER = ";";

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
        } catch (IOException | NumberFormatException e) { return false; }
        return false;
    }

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
        } catch (IOException | NumberFormatException e) { }
        return ucs;
    }

    public boolean existe(String sigla) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(DELIMITER);
                if (dados.length > 0 && dados[0].equalsIgnoreCase(sigla)) return true;
            }
        } catch (IOException e) { return false; }
        return false;
    }

    public boolean adicionar(UnidadeCurricular uc) {
        String linha = String.join(DELIMITER,
                uc.getSigla(), uc.getNome(), uc.getSiglaCurso(), String.valueOf(uc.getAnoCurricular()), String.valueOf(uc.getIdDocenteResponsavel())
        ) + System.lineSeparator();

        File dir = new File("data");
        if (!dir.exists()) dir.mkdir();

        try {
            Files.write(Paths.get(FILE_PATH), linha.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) { return false; }
    }

    public int contarPorDocente(int idDocente) {
        int count = 0;
        File f = new File(FILE_PATH);
        if (!f.exists()) return 0;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(DELIMITER);
                if (dados.length >= 5 && Integer.parseInt(dados[4]) == idDocente) count++;
            }
        } catch (IOException | NumberFormatException e) { return 0; }
        return count;
    }

    public int contarPorCursoEAno(String siglaCurso, int ano) {
        int count = 0;
        File f = new File(FILE_PATH);
        if (!f.exists()) return 0;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(DELIMITER);
                if (dados.length >= 5 && dados[2].equalsIgnoreCase(siglaCurso) && Integer.parseInt(dados[3]) == ano) {
                    count++;
                }
            }
        } catch (IOException | NumberFormatException e) { return 0; }
        return count;
    }
}