package common;

import java.security.SecureRandom;

/**
 * Gera passwords aleatórias seguras usando SecureRandom.
 */
public final class PasswordGenerator {

    private static final String CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";

    private PasswordGenerator() {}

    /**
     * Gera uma password aleatória com o comprimento indicado.
     * @param comprimento Número de caracteres (mínimo 6).
     * @return Password gerada.
     */
    public static String gerarPassword(int comprimento) {
        if (comprimento < 6) comprimento = 6;
        SecureRandom rng = new SecureRandom();
        StringBuilder sb = new StringBuilder(comprimento);
        for (int i = 0; i < comprimento; i++) {
            sb.append(CHARS.charAt(rng.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}