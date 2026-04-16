package model;

public class Gestor {
    private int id;
    private String nome;
    private String email;
    private String passwordHash;

    public Gestor(int id, String nome, String email, String passwordHash) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }

    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}