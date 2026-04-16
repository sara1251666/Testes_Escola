package common;

public class EmailGenerator {
    /**
     * Gera e-mail para estudantes baseado no número mecanográfico.
     */
    public static String gerarEmailEstudante(int numeroMecanografico) {
        return numeroMecanografico + "@issmf.ipp.pt";
    }

    /**
     * Gera e-mail para docentes baseado na sua sigla.
     */
    public static String gerarEmailDocente(String sigla) {
        return sigla.toLowerCase() + "@issmf.ipp.pt";
    }
}