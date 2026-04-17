package model;

/**
 * Singleton que guarda o estado da sessão ativa.
 *
 * Dados guardados após login bem-sucedido:
 *  - tipo:          "ESTUDANTE", "DOCENTE" ou "GESTOR"
 *  - email:         email do utilizador (chave nas credenciais)
 *  - idNumerico:    numMec do estudante, ou 1 para gestor; -1 para docente
 *  - siglaDocente:  sigla do docente (ex: "ABC"); null para os outros tipos
 *  - anoAtual:      ano letivo em curso
 */
public class RepositorioSessao {

    private static RepositorioSessao instancia;

    private String tipo;
    private String email;
    private int    idNumerico    = -1;
    private String siglaDocente;
    private int    anoAtual      = 2026;

    private RepositorioSessao() {}

    public static RepositorioSessao getInstance() {
        if (instancia == null) {
            instancia = new RepositorioSessao();
        }
        return instancia;
    }

    // --- Getters ---
    public String getTipoUtilizadorLogado()  { return tipo;          }
    public String getEmailUtilizadorLogado() { return email;         }
    public int    getIdUtilizadorLogado()    { return idNumerico;    }
    public String getSiglaDocenteLogado()    { return siglaDocente;  }
    public int    getAnoAtual()              { return anoAtual;      }

    // --- Setters ---
    public void setTipoUtilizadorLogado(String tipo)     { this.tipo          = tipo;     }
    public void setEmailUtilizadorLogado(String email)   { this.email         = email;    }
    public void setIdUtilizadorLogado(int id)            { this.idNumerico    = id;       }
    public void setSiglaDocenteLogado(String sigla)      { this.siglaDocente  = sigla;    }
    public void setAnoAtual(int ano)                     { this.anoAtual      = ano;      }

    /** Limpa todos os dados da sessão no logout. */
    public void limparSessao() {
        this.tipo         = null;
        this.email        = null;
        this.idNumerico   = -1;
        this.siglaDocente = null;
    }

    public boolean estaLogado() {
        return tipo != null;
    }
}
