package model;

/**
 * Entidade Estudante.
 *
 * CSV: numMec;email;nome;nif;morada;dataNascimento;anoInscricao;siglaCurso;saldoDevedor;anoCurricular;ativo
 * Idx:   0      1     2    3    4         5              6          7           8             9          10
 *
 * O campo 'ativo' é opcional na leitura (retrocompatibilidade):
 * se estiver ausente, assume-se true.
 */
public class Estudante {

    private final int    numMec;
    private String email;
    private String nome;
    private String nif;
    private String morada;
    private String dataNascimento;
    private final int    anoInscricao;
    private final String siglaCurso;
    private double saldoDevedor;
    private int    anoCurricular;
    private boolean ativo;

    /** Construtor completo — usado pelo DAL ao ler do CSV (com campo ativo). */
    public Estudante(int numMec, String email, String nome, String nif,
                     String morada, String dataNascimento, int anoInscricao,
                     String siglaCurso, double saldoDevedor, int anoCurricular, boolean ativo) {
        this.numMec         = numMec;
        this.email          = email;
        this.nome           = nome;
        this.nif            = nif;
        this.morada         = morada;
        this.dataNascimento = dataNascimento;
        this.anoInscricao   = anoInscricao;
        this.siglaCurso     = siglaCurso;
        this.saldoDevedor   = saldoDevedor;
        this.anoCurricular  = anoCurricular;
        this.ativo          = ativo;
    }

    /** Construtor retrocompatível (10 campos) — ativo=true por defeito. */
    public Estudante(int numMec, String email, String nome, String nif,
                     String morada, String dataNascimento, int anoInscricao,
                     String siglaCurso, double saldoDevedor, int anoCurricular) {
        this(numMec, email, nome, nif, morada, dataNascimento, anoInscricao,
             siglaCurso, saldoDevedor, anoCurricular, true);
    }

    /** Construtor de novo registo — saldo=propina do curso, anoCurricular=1, ativo=true. */
    public Estudante(int numMec, String email, String nome, String nif,
                     String morada, String dataNascimento, int anoInscricao, String siglaCurso) {
        this(numMec, email, nome, nif, morada, dataNascimento, anoInscricao, siglaCurso, 0.0, 1, true);
    }

    // Getters
    public int     getNumMec()          { return numMec;         }
    public String  getEmail()           { return email;          }
    public String  getNome()            { return nome;           }
    public String  getNif()             { return nif;            }
    public String  getMorada()          { return morada;         }
    public String  getDataNascimento()  { return dataNascimento; }
    public int     getAnoInscricao()    { return anoInscricao;   }
    public String  getSiglaCurso()      { return siglaCurso;     }
    public double  getSaldoDevedor()    { return saldoDevedor;   }
    public int     getAnoCurricular()   { return anoCurricular;  }
    public boolean isAtivo()            { return ativo;          }

    // Setters (só campos mutáveis)
    public void setMorada(String morada)       { this.morada        = morada;  }
    public void setSaldoDevedor(double saldo)  { this.saldoDevedor  = saldo;   }
    public void setAnoCurricular(int ano)      { this.anoCurricular = ano;     }
    public void setAtivo(boolean ativo)        { this.ativo         = ativo;   }

    /**
     * Serializa para linha CSV.
     * numMec;email;nome;nif;morada;dataNascimento;anoInscricao;siglaCurso;saldoDevedor;anoCurricular;ativo
     */
    public String toCSV() {
        return numMec + ";" + email + ";" + nome + ";" + nif + ";" + morada + ";"
                + dataNascimento + ";" + anoInscricao + ";" + siglaCurso + ";"
                + saldoDevedor + ";" + anoCurricular + ";" + ativo;
    }

    @Override
    public String toString() {
        return "Estudante{" + numMec + ", " + nome + ", " + siglaCurso
                + ", ano=" + anoCurricular + ", saldo=" + saldoDevedor
                + ", ativo=" + ativo + "}";
    }
}
