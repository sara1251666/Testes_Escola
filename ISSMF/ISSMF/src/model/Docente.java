package model;

/**
 * Entidade Docente.
 *
 * CSV: sigla;email;nome;nif;morada;dataNascimento
 * O identificador único é a SIGLA (3 letras maiúsculas, ex: "ABC"), não um inteiro.
 */
public class Docente {

    private final String sigla;
    private String email;
    private String nome;
    private String nif;
    private String morada;
    private String dataNascimento;

    public Docente(String sigla, String email, String nome,
                   String nif, String morada, String dataNascimento) {
        this.sigla          = sigla.toUpperCase();
        this.email          = email;
        this.nome           = nome;
        this.nif            = nif;
        this.morada         = morada;
        this.dataNascimento = dataNascimento;
    }

    // Getters
    public String getSigla()          { return sigla;          }
    public String getEmail()          { return email;          }
    public String getNome()           { return nome;           }
    public String getNif()            { return nif;            }
    public String getMorada()         { return morada;         }
    public String getDataNascimento() { return dataNascimento; }

    /** sigla;email;nome;nif;morada;dataNascimento */
    public String toCSV() {
        return sigla + ";" + email + ";" + nome + ";" + nif + ";" + morada + ";" + dataNascimento;
    }
}