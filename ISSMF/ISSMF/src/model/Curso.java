package model;

public class Curso {
    private String sigla;
    private String nome;
    private String siglaDepartamento;
    private double propina;
    private String estado;

    public Curso(String sigla, String nome, String siglaDepartamento, double propina, String estado) {
        this.sigla = sigla;
        this.nome = nome;
        this.siglaDepartamento = siglaDepartamento;
        this.propina = propina;
        this.estado = estado;
    }

    public String getSigla() { return sigla; }
    public String getNome() { return nome; }
    public double getPropina() { return propina; }
    public String getEstado() { return estado; }
}