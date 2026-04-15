package dal;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class AutenticacaoDAL {
    private static final String FILE_CREDENCIAIS = "data/credenciais.csv";
    private static final String DELIMITER = ";";

    public String[] validarCredenciais(String email, String passwordHash) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_CREDENCIAIS))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(DELIMITER);
                if (dados.length >= 4 && dados[0].equalsIgnoreCase(email)) {
                    if (dados[1].equals(passwordHash)) {
                        return new String[]{dados[2], dados[3]};
                    } else {
                        return null;
                    }
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    public boolean existeEmail(String email) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_CREDENCIAIS))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(DELIMITER);
                if (dados.length > 0 && dados[0].equalsIgnoreCase(email)) {
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    public boolean atualizarPassword(String email, String novaPasswordHash) {
        File ficheiroOriginal = new File(FILE_CREDENCIAIS);
        File ficheiroTemp = new File("data/credenciais_temp.csv");
        boolean encontrado = false;

        try (BufferedReader br = new BufferedReader(new FileReader(ficheiroOriginal));
             BufferedWriter bw = new BufferedWriter(new FileWriter(ficheiroTemp))) {

            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(DELIMITER);
                if (dados.length >= 4 && dados[0].equalsIgnoreCase(email)) {
                    String novaLinha = String.join(DELIMITER, dados[0], novaPasswordHash, dados[2], dados[3]);
                    bw.write(novaLinha);
                    encontrado = true;
                } else {
                    bw.write(linha);
                }
                bw.newLine();
            }
        } catch (IOException e) {
            return false;
        }

        return substituirFicheiro(encontrado, ficheiroOriginal, ficheiroTemp);
    }

    public boolean atualizarPasswordPorId(int idUtilizador, String novaPasswordHash) {
        File ficheiroOriginal = new File(FILE_CREDENCIAIS);
        File ficheiroTemp = new File("data/credenciais_temp.csv");
        boolean encontrado = false;

        try (BufferedReader br = new BufferedReader(new FileReader(ficheiroOriginal));
             BufferedWriter bw = new BufferedWriter(new FileWriter(ficheiroTemp))) {

            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(DELIMITER);
                if (dados.length >= 4) {
                    try {
                        int idLinha = Integer.parseInt(dados[3]);
                        if (idLinha == idUtilizador) {
                            String novaLinha = String.join(DELIMITER, dados[0], novaPasswordHash, dados[2], dados[3]);
                            bw.write(novaLinha);
                            encontrado = true;
                        } else {
                            bw.write(linha);
                        }
                    } catch (NumberFormatException e) {
                        bw.write(linha);
                    }
                } else {
                    bw.write(linha);
                }
                bw.newLine();
            }
        } catch (IOException e) {
            return false;
        }

        return substituirFicheiro(encontrado, ficheiroOriginal, ficheiroTemp);
    }

    private boolean substituirFicheiro(boolean sucesso, File original, File temporario) {
        if (sucesso) {
            try {
                Files.move(temporario.toPath(), original.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return true;
            } catch (IOException e) {
                return false;
            }
        } else {
            temporario.delete();
            return false;
        }
    }

    public boolean criarCredencial(String email, String hash, String tipo) {
        String linha = email + ";" + hash + ";" + tipo + System.lineSeparator();
        try {
            java.nio.file.Files.write(java.nio.file.Paths.get("data/credenciais.csv"),
                    linha.getBytes(), java.nio.file.StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) { return false; }
    }
}