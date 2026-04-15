package dal;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class CursoDAL {
    private static final String FILE_PATH = "data/cursos.csv";
    private static final String DELIMITER = ";";

    public boolean existeCurso(String sigla) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(DELIMITER);
                if (dados.length > 0 && dados[0].equalsIgnoreCase(sigla)) {
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    public boolean adicionarCurso(String sigla, String nome, String departamento) {
        String linha = String.join(DELIMITER, sigla, nome, departamento) + System.lineSeparator();

        File dir = new File("data");
        if (!dir.exists()) dir.mkdir();

        try {
            Files.write(Paths.get(FILE_PATH), linha.getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean atualizarCurso(String sigla, String novoNome, String novoDep) {
        File ficheiroOriginal = new File(FILE_PATH);
        File ficheiroTemp = new File("data/cursos_temp.csv");
        boolean atualizado = false;

        try (BufferedReader br = new BufferedReader(new FileReader(ficheiroOriginal));
             BufferedWriter bw = new BufferedWriter(new FileWriter(ficheiroTemp))) {

            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(DELIMITER);

                if (dados.length > 0 && dados[0].equalsIgnoreCase(sigla)) {
                    String novaLinha = String.join(DELIMITER, sigla, novoNome, novoDep);
                    bw.write(novaLinha);
                    atualizado = true;
                } else {
                    bw.write(linha);
                }
                bw.newLine();
            }
        } catch (IOException e) {
            return false;
        }

        if (atualizado) {
            try {
                Files.move(ficheiroTemp.toPath(), ficheiroOriginal.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return true;
            } catch (IOException e) {
                return false;
            }
        } else {
            ficheiroTemp.delete();
            return false;
        }
    }

    /**
     * Conta o número total de cursos registados no ficheiro.
     */
    public int contarTotalCursos() {
        int contador = 0;
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(FILE_PATH))) {
            while (br.readLine() != null) {
                contador++;
            }
        } catch (java.io.IOException e) {
            return 0;
        }
        return contador;
    }
}