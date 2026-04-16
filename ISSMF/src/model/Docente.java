package model;

/**
 * Entidade Docente.
 *
 * CSV: sigla;email;nome;nif;morada;dataNascimento;ativo
 * Idx:   0     1     2    3    4         5          6
 *
 * O campo 'ativo' é opcional na leitura (retrocompatibilidade):
 * se estiver ausente, assume-se true.
 *
 * O identificador único é a SIGLA (3 letras maiúsculas, ex: "ABC"), não um inteiro.
 */
public class Docente {

    private final String  sigla;
    private String  email;
    private String  nome;
    private String  nif;
    private String  morada;
    private String  dataNascimento;
    private boolean ativo;

    /** Construtor completo — usado pelo DAL ao ler do CSV. */
    public Docente(String sigla, String email, String nome,
                   String nif, String morada, String dataNascimento, boolean ativo) {
        this.sigla          = sigla.toUpperCase();
        this.email          = email;
        this.nome           = nome;
        this.nif            = nif;
        this.morada         = morada;
        this.dataNascimento = dataNascimento;
        this.ativo          = ativo;
    }

    /** Construtor de novo registo — ativo=true por defeito. */
    public Docente(String sigla, String email, String nome,
                   String nif, String morada, String dataNascimento) {
        this(sigla, email, nome, nif, morada, dataNascimento, true);
    }

    // Getters
    public String  getSigla()          { return sigla;          }
    public String  getEmail()          { return email;          }
    public String  getNome()           { return nome;           }
    public String  getNif()            { return nif;            }
    public String  getMorada()         { return morada;         }
    public String  getDataNascimento() { return dataNascimento; }
    public boolean isAtivo()           { return ativo;          }

    // Setters
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    /** sigla;email;nome;nif;morada;dataNascimento;ativo */
    public String toCSV() {
        return sigla + ";" + email + ";" + nome + ";" + nif + ";" + morada + ";"
                + dataNascimento + ";" + ativo;
    }
}