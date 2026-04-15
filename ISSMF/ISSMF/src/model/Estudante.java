package model;

public class Estudante {
    private int numMec;
    private String nif;
    private String nome;
    private String email;
    private String morada;
    private int idCurso;
    private double saldoDevedor;

    public Estudante(int numMec, String nif, String nome, String email, String morada, int idCurso, double saldoDevedor) {
        this.numMec = numMec;
        this.nif = nif;
        this.nome = nome;
        this.email = email;
        this.morada = morada;
        this.idCurso = idCurso;
        this.saldoDevedor = saldoDevedor;
    }

    public Estudante(String nif, String nome, String email, int idCurso) {
        this.numMec = (int) (Math.random() * 10000);
        this.nif = nif;
        this.nome = nome;
        this.email = email;
        this.idCurso = idCurso;
        this.morada = "Não definida";
        this.saldoDevedor = 0.0; // Inicia sem dívidas
    }

    // Getters
    public int getNumMec() { return numMec; }
    public String getNif() { return nif; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getMorada() { return morada; }
    public int getIdCurso() { return idCurso; }
    public double getSaldoDevedor() { return saldoDevedor; }


    // Verifica se os teus atributos se chamam assim:
    // private double saldoDevedor;
    // private String morada;

    public void setMorada(String morada) {
        this.morada = morada;
    }

    public void setSaldoDevedor(double saldoDevedor) {
        this.saldoDevedor = saldoDevedor;
    }

    public String toCSV() {
        return numMec + ";" + nif + ";" + nome + ";" + email + ";" + morada + ";" + idCurso + ";" + saldoDevedor;
    }
}