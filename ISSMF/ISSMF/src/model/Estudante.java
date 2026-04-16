package model;

/**
 * Entidade Estudante.
 *
 * CSV: numMec;email;nome;nif;morada;dataNascimento;anoInscricao;siglaCurso;saldoDevedor;anoCurricular
 * Idx:   0      1     2    3    4         5              6          7           8             9
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

    /** Construtor completo — usado pelo DAL ao ler do CSV. */
    public Estudante(int numMec, String email, String nome, String nif,
                     String morada, String dataNascimento, int anoInscricao,
                     String siglaCurso, double saldoDevedor, int anoCurricular) {
        this.numMec        = numMec;
        this.email         = email;
        this.nome          = nome;
        this.nif           = nif;
        this.morada        = morada;
        this.dataNascimento= dataNascimento;
        this.anoInscricao  = anoInscricao;
        this.siglaCurso    = siglaCurso;
        this.saldoDevedor  = saldoDevedor;
        this.anoCurricular = anoCurricular;
    }

    /** Construtor de novo registo — saldo=0, anoCurricular=1. */
    public Estudante(int numMec, String email, String nome, String nif,
                     String morada, String dataNascimento, int anoInscricao, String siglaCurso) {
        this(numMec, email, nome, nif, morada, dataNascimento, anoInscricao, siglaCurso, 0.0, 1);
    }

    // Getters
    public int    getNumMec()          { return numMec;         }
    public String getEmail()           { return email;          }
    public String getNome()            { return nome;           }
    public String getNif()             { return nif;            }
    public String getMorada()          { return morada;         }
    public String getDataNascimento()  { return dataNascimento; }
    public int    getAnoInscricao()    { return anoInscricao;   }
    public String getSiglaCurso()      { return siglaCurso;     }
    public double getSaldoDevedor()    { return saldoDevedor;   }
    public int    getAnoCurricular()   { return anoCurricular;  }

    // Setters (só campos mutáveis)
    public void setMorada(String morada)              { this.morada        = morada;   }
    public void setSaldoDevedor(double saldo)         { this.saldoDevedor  = saldo;    }
    public void setAnoCurricular(int ano)             { this.anoCurricular = ano;      }

    /**
     * Serializa para linha CSV.
     * numMec;email;nome;nif;morada;dataNascimento;anoInscricao;siglaCurso;saldoDevedor;anoCurricular
     */
    public String toCSV() {
        return numMec + ";" + email + ";" + nome + ";" + nif + ";" + morada + ";"
                + dataNascimento + ";" + anoInscricao + ";" + siglaCurso + ";"
                + saldoDevedor + ";" + anoCurricular;
    }

    @Override
    public String toString() {
        return "Estudante{" + numMec + ", " + nome + ", " + siglaCurso
                + ", ano=" + anoCurricular + ", saldo=" + saldoDevedor + "}";
    }
}
