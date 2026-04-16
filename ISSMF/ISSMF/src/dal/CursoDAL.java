package dal;

import model.Curso;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class CursoDAL {
    private static final String FILE_PATH = "data/cursos.csv";
    private static final String DELIMITER = ";";

    // MUDOU DE existeCurso PARA existe
    public boolean existe(String sigla) {
        return obterPorSigla(sigla) != null;
    }

    // MÉTODO NOVO: Necessário para a edição de cursos
    public Curso obterPorSigla(String sigla) {
        File f = new File(FILE_PATH);
        if (!f.exists()) return null;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(DELIMITER);
                if (dados.length >= 3 && dados[0].equalsIgnoreCase(sigla)) {
                    return new Curso(dados[0], dados[1], dados[2]);
                }
            }
        } catch (IOException e) { return null; }
        return null;
    }

    // MUDOU DE adicionarCurso PARA adicionar
    public boolean adicionar(Curso c) {
        String linha = String.join(DELIMITER, c.getSigla(), c.getNome(), c.getDepartamento()) + System.lineSeparator();

        File dir = new File("data");
        if (!dir.exists()) dir.mkdir();

        try {
            Files.write(Paths.get(FILE_PATH), linha.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // MUDOU DE atualizarCurso PARA atualizar
    public boolean atualizar(Curso c) {
        File ficheiroOriginal = new File(FILE_PATH);
        File ficheiroTemp = new File("data/cursos_temp.csv");
        boolean atualizado = false;

        try (BufferedReader br = new BufferedReader(new FileReader(ficheiroOriginal));
             BufferedWriter bw = new BufferedWriter(new FileWriter(ficheiroTemp))) {

            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(DELIMITER);

                if (dados.length > 0 && dados[0].equalsIgnoreCase(c.getSigla())) {
                    String novaLinha = String.join(DELIMITER, c.getSigla(), c.getNome(), c.getDepartamento());
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
            } catch (IOException e) { return false; }
        } else {
            ficheiroTemp.delete();
            return false;
        }
    }

    public int contarTotal() {
        int contador = 0;
        File f = new File(FILE_PATH);
        if (!f.exists()) return 0;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            br.readLine(); // Pular o cabeçalho se existir
            while (br.readLine() != null) {
                contador++;
            }
        } catch (IOException e) { return 0; }
        return contador;
    }
}