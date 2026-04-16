package model;

/**
 * Entidade Curso.
 *
 * CSV: sigla;nome;siglaDepartamento;propina;estado
 * estado: "Ativo" | "Inativo"
 */
public class Curso {

    private final String sigla;
    private String nome;
    private String siglaDepartamento;
    private double propina;
    private String estado;

    public Curso(String sigla, String nome, String siglaDepartamento,
                 double propina, String estado) {
        this.sigla             = sigla.toUpperCase();
        this.nome              = nome;
        this.siglaDepartamento = siglaDepartamento.toUpperCase();
        this.propina           = propina;
        this.estado            = estado;
    }

    // Getters
    public String getSigla()             { return sigla;             }
    public String getNome()              { return nome;              }
    public String getSiglaDepartamento() { return siglaDepartamento; }
    public double getPropina()           { return propina;           }
    public String getEstado()            { return estado;            }

    // Setters (o gestor pode editar, se não houver alunos)
    public void setNome(String nome)                      { this.nome              = nome;   }
    public void setSiglaDepartamento(String siglaDep)     { this.siglaDepartamento = siglaDep; }
    public void setPropina(double propina)                { this.propina           = propina; }
    public void setEstado(String estado)                  { this.estado            = estado; }

    public boolean estaAtivo() { return "Ativo".equalsIgnoreCase(estado); }

    /** sigla;nome;siglaDepartamento;propina;estado */
    public String toCSV() {
        return sigla + ";" + nome + ";" + siglaDepartamento + ";" + propina + ";" + estado;
    }
}