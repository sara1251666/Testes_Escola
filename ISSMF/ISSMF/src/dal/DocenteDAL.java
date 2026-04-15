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
}