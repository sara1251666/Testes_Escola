package model;

public class UC {
    private String sigla;
    private String nome;
    private int anoCurricular;
    private String siglaDocente;
    private String siglaCurso;

    public UC(String sigla, String nome, int anoCurricular, String siglaDocente, String siglaCurso) {
        this.sigla = sigla;
        this.nome = nome;
        this.anoCurricular = anoCurricular;
        this.siglaDocente = siglaDocente;
        this.siglaCurso = siglaCurso;
    }

    public String getSigla() { return sigla; }
    public String getSiglaDocente() { return siglaDocente; }
}