package model;

/**
 * Entidade Avaliação.
 *
 * CSV: numMec;siglaUC;anoLetivo;notaNormal;notaRecurso;notaEspecial
 * Idx:    0      1        2         3           4           5
 *
 * Uma nota de -1 significa falta/ausência.
 * Aprovação: pelo menos uma nota >= 9.5 (Normal, Recurso ou Especial).
 */
public class Avaliacao {
    private final int    numMec;
    private final String siglaUC;
    private final int    anoLetivo;
    private final double notaNormal;
    private final double notaRecurso;
    private final double notaEspecial;

    public Avaliacao(int numMec, String siglaUC, int anoLetivo,
                     double notaNormal, double notaRecurso, double notaEspecial) {
        this.numMec      = numMec;
        this.siglaUC     = siglaUC;
        this.anoLetivo   = anoLetivo;
        this.notaNormal  = notaNormal;
        this.notaRecurso = notaRecurso;
        this.notaEspecial= notaEspecial;
    }

    // Getters
    public int    getNumMec()       { return numMec;       }
    public String getSiglaUC()      { return siglaUC;      }
    public int    getAnoLetivo()    { return anoLetivo;    }
    public double getNotaNormal()   { return notaNormal;   }
    public double getNotaRecurso()  { return notaRecurso;  }
    public double getNotaEspecial() { return notaEspecial; }

    /**
     * Verifica se o estudante está aprovado na UC.
     * Aprovado = pelo menos uma nota válida (>= 0) e >= 9.5.
     */
    public boolean estaAprovado() {
        return (notaNormal  >= 0 && notaNormal  >= 9.5)
            || (notaRecurso >= 0 && notaRecurso >= 9.5)
            || (notaEspecial>= 0 && notaEspecial>= 9.5);
    }

    /** Melhor nota obtida (ignora ausências). */
    public double melhorNota() {
        double melhor = -1;
        if (notaNormal  >= 0) melhor = Math.max(melhor, notaNormal);
        if (notaRecurso >= 0) melhor = Math.max(melhor, notaRecurso);
        if (notaEspecial>= 0) melhor = Math.max(melhor, notaEspecial);
        return melhor;
    }

    @Override
    public String toString() {
        String norm = notaNormal  == -1 ? "Falta" : String.valueOf(notaNormal);
        String rec  = notaRecurso == -1 ? "Falta" : String.valueOf(notaRecurso);
        String esp  = notaEspecial== -1 ? "Falta" : String.valueOf(notaEspecial);
        return String.format("UC: %-8s | Normal: %-6s | Recurso: %-6s | Especial: %-6s | %s",
                siglaUC, norm, rec, esp, estaAprovado() ? "APROVADO" : "REPROVADO");
    }
}
