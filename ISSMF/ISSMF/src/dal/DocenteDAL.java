package dal;

import model.Docente;
import java.io.*;

public class DocenteDAL {
    private static final String FILE_PATH = "data/docentes.csv";
    private static final String DELIMITER = ";";

    public Docente obterDocentePorId(int idDocente) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String linha = br.readLine();

            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(DELIMITER);

                if (dados.length >= 6 && Integer.parseInt(dados[0]) == idDocente) {
                    return new Docente(
                            dados[0],
                            dados[1],
                            dados[2],
                            dados[3],
                            dados[4],
                            dados[5]
                    );
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Erro ao ler docente: " + e.getMessage());
            return null;
        }
        return null;
    }

    public boolean existeNif(String nif) {
        java.io.File f = new java.io.File(FILE_PATH);
        if (!f.exists()) return false;
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(f))) {
            String linha = br.readLine(); // Pular cabeçalho
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(DELIMITER);
                if (dados.length >= 6 && dados[3].equals(nif)) return true;
            }
        } catch (java.io.IOException e) { return false; }
        return false;
    }

    public boolean existeSigla(String sigla) {
        return obterPorSigla(sigla) != null;
    }

    public Docente obterPorSigla(String sigla) {
        java.io.File f = new java.io.File(FILE_PATH);
        if (!f.exists()) return null;
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(f))) {
            String linha = br.readLine(); // Pular cabeçalho
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(DELIMITER);
                if (dados.length >= 6 && dados[0].equalsIgnoreCase(sigla)) {
                    return new Docente(dados[0], dados[1], dados[2], dados[3], dados[4], dados[5]);
                }
            }
        } catch (java.io.IOException e) { return null; }
        return null;
    }

    public boolean adicionar(Docente docente) {
        try (java.io.PrintWriter out = new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter(FILE_PATH, true)))) {
            out.println(docente.toCSV());
            return true;
        } catch (java.io.IOException ex) {
            return false;
        }
    }
}