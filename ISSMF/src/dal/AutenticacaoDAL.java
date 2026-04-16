package dal;

import common.SecurityUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * DAL para autenticação.
 *
 * Formato CSV: email;passwordHash;tipo;idOuSigla
 *   - ESTUDANTE: idOuSigla = numMec (ex: 20260001)
 *   - DOCENTE:   idOuSigla = sigla  (ex: ABC)
 *   - GESTOR:    idOuSigla = 1
 *
 * A verificação de password usa SecurityUtil.verificarPassword() que
 * extrai o salt do hash armazenado — nunca re-hasheia com salt novo.
 */
public class AutenticacaoDAL {
    private static final String FILE_CREDENCIAIS = "data/credenciais.csv";
    private static final String DELIMITER = ";";

    /**
     * Valida credenciais por email + password plaintext.
     * @return String[]{tipo, idOuSigla} se válido, null caso contrário.
     */
    public String[] validarCredenciais(String email, String passwordPlain) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_CREDENCIAIS))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                // Ignorar cabeçalho ou linhas inválidas
                if (linha.startsWith("email") || linha.isBlank()) continue;

                String[] dados = linha.split(DELIMITER, 4);
                if (dados.length >= 3 && dados[0].equalsIgnoreCase(email)) {
                    String storedHash = dados[1];
                    if (SecurityUtil.verificarPassword(passwordPlain, storedHash)) {
                        String tipo      = dados[2];
                        String idSigla   = dados.length >= 4 ? dados[3].trim() : "0";
                        return new String[]{tipo, idSigla};
                    } else {
                        return null; // Email encontrado mas password errada
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[AutenticacaoDAL] Erro ao ler credenciais: " + e.getMessage());
        }
        return null;
    }

    public boolean existeEmail(String email) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_CREDENCIAIS))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.startsWith("email") || linha.isBlank()) continue;
                String[] dados = linha.split(DELIMITER, 4);
                if (dados.length > 0 && dados[0].equalsIgnoreCase(email)) return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    /** Atualiza a password por email. */
    public boolean atualizarPassword(String email, String novaPasswordHash) {
        return substituirNoFicheiro(email, novaPasswordHash);
    }

    /** Atualiza a password por sigla do docente (coluna idOuSigla). */
    public boolean atualizarPasswordPorSigla(String siglaDocente, String novaPasswordHash) {
        File orig = new File(FILE_CREDENCIAIS);
        File temp = new File("data/credenciais_temp.csv");
        boolean encontrado = false;

        try (BufferedReader br = new BufferedReader(new FileReader(orig));
             BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank()) { bw.newLine(); continue; }
                String[] dados = linha.split(DELIMITER, 4);
                boolean ehDocente = dados.length >= 4
                        && dados[2].equalsIgnoreCase("DOCENTE")
                        && dados[3].trim().equalsIgnoreCase(siglaDocente);
                if (ehDocente) {
                    bw.write(String.join(DELIMITER, dados[0], novaPasswordHash, dados[2], dados[3].trim()));
                    encontrado = true;
                } else {
                    bw.write(linha);
                }
                bw.newLine();
            }
        } catch (IOException e) { temp.delete(); return false; }

        return mover(encontrado, orig, temp);
    }

    /**
     * Cria uma nova credencial.
     * @param email    Email do utilizador.
     * @param hash     Hash da password (formato base64salt:base64hash).
     * @param tipo     "ESTUDANTE", "DOCENTE" ou "GESTOR".
     * @param idSigla  numMec para estudante, sigla para docente, "1" para gestor.
     */
    public boolean criarCredencial(String email, String hash, String tipo, String idSigla) {
        new File("data").mkdirs();
        String linha = String.join(DELIMITER, email, hash, tipo, idSigla) + System.lineSeparator();
        try {
            Files.write(java.nio.file.Paths.get(FILE_CREDENCIAIS),
                    linha.getBytes(), java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            System.err.println("[AutenticacaoDAL] Erro ao criar credencial: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // PRIVADOS
    // =========================================================================

    private boolean substituirNoFicheiro(String email, String novaHash) {
        File orig = new File(FILE_CREDENCIAIS);
        File temp = new File("data/credenciais_temp.csv");
        boolean encontrado = false;

        try (BufferedReader br = new BufferedReader(new FileReader(orig));
             BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank()) { bw.newLine(); continue; }
                String[] dados = linha.split(DELIMITER, 4);
                if (dados.length >= 3 && dados[0].equalsIgnoreCase(email)) {
                    String idParte = dados.length >= 4 ? dados[3].trim() : "0";
                    bw.write(String.join(DELIMITER, dados[0], novaHash, dados[2], idParte));
                    encontrado = true;
                } else {
                    bw.write(linha);
                }
                bw.newLine();
            }
        } catch (IOException e) { temp.delete(); return false; }

        return mover(encontrado, orig, temp);
    }

    private boolean mover(boolean sucesso, File orig, File temp) {
        if (sucesso) {
            try {
                Files.move(temp.toPath(), orig.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return true;
            } catch (IOException e) { return false; }
        }
        temp.delete();
        return false;
    }
}