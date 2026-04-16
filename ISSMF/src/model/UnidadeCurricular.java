package model;

public class UnidadeCurricular {
    private String sigla;
    private String nome;
    private String siglaCurso;
    private int anoCurricular; // 1º, 2º ou 3º ano
    private int idDocenteResponsavel;

    public UnidadeCurricular(String sigla, String nome, String siglaCurso, int anoCurricular, int idDocenteResponsavel) {
        this.sigla = sigla;
        this.nome = nome;
        this.siglaCurso = siglaCurso;
        this.anoCurricular = anoCurricular;
        this.idDocenteResponsavel = idDocenteResponsavel;
    }

    public String getSigla() { return sigla; }
    public String getNome() { return nome; }
    public String getSiglaCurso() { return siglaCurso; }
    public int getAnoCurricular() { return anoCurricular; }
    public int getIdDocenteResponsavel() { return idDocenteResponsavel; }
}