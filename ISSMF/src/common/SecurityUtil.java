package common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Utilitário de segurança para passwords.
 *
 * FORMATO ARMAZENADO NO CSV: "base64(salt):base64(hash)"
 * Exemplo: "yEBC9iGrEu0xL0gj/2+FCw==:TnU+uPlyVZQEaLzUp4mUN6G2Q..."
 *
 * PORQUÊ SALT?
 *   O salt garante que dois utilizadores com a mesma password têm hashes
 *   diferentes no CSV, tornando ataques de dicionário inviáveis.
 */
public final class SecurityUtil {

    private static final String ALGORITHM   = "SHA-256";
    private static final int    SALT_BYTES  = 16;

    private SecurityUtil() {}

    /**
     * Gera um hash seguro e salteado de uma password em plaintext.
     * @param plainPassword A password em texto simples.
     * @return String no formato "base64Salt:base64Hash", pronta para gravar no CSV.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password não pode ser nula ou vazia.");
        }
        try {
            byte[] salt = gerarSalt();
            byte[] hash = calcularHash(plainPassword, salt);
            return Base64.getEncoder().encodeToString(salt)
                    + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo " + ALGORITHM + " não disponível.", e);
        }
    }

    /**
     * Verifica se uma password em plaintext corresponde ao hash armazenado.
     *
     * @param plainPassword  A password digitada pelo utilizador.
     * @param storedHash     O valor guardado no CSV ("base64Salt:base64Hash").
     * @return true se a password é válida, false caso contrário.
     */
    public static boolean verificarPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) return false;

        String[] partes = storedHash.split(":", 2);
        if (partes.length != 2) return false;

        try {
            byte[] salt          = Base64.getDecoder().decode(partes[0]);
            byte[] hashEsperado  = Base64.getDecoder().decode(partes[1]);
            byte[] hashCalculado = calcularHash(plainPassword, salt);

            // Comparação em tempo constante — previne timing attacks
            return MessageDigest.isEqual(hashEsperado, hashCalculado);

        } catch (IllegalArgumentException | NoSuchAlgorithmException e) {
            // Base64 inválido ou algoritmo ausente
            return false;
        }
    }

    private static byte[] gerarSalt() {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private static byte[] calcularHash(String password, byte[] salt)
            throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
        digest.update(salt);
        return digest.digest(password.getBytes());
    }
}