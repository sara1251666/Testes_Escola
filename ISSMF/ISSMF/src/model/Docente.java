package model;

public class Docente {
    private String sigla;
    private String email;
    private String nome;
    private String nif;
    private String morada;
    private String dataNascimento;

    public Docente(String sigla, String email, String nome, String nif, String morada, String dataNascimento) {
        this.sigla = sigla;
        this.email = email;
        this.nome = nome;
        this.nif = nif;
        this.morada = morada;
        this.dataNascimento = dataNascimento;
    }

    public String getSigla() { return sigla; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
}