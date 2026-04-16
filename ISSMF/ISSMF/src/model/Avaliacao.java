package model;

public class Avaliacao {
    private int idAluno;
    private String siglaUc;
    private int anoLetivo;
    private double notaNormal;
    private double notaRecurso;
    private double notaEspecial;

    public Avaliacao(int idAluno, String siglaUc, int anoLetivo, double notaNormal, double notaRecurso, double notaEspecial) {
        this.idAluno = idAluno;
        this.siglaUc = siglaUc;
        this.anoLetivo = anoLetivo;
        this.notaNormal = notaNormal;
        this.notaRecurso = notaRecurso;
        this.notaEspecial = notaEspecial;
    }

    public int getIdAluno() { return idAluno; }
    public String getSiglaUc() { return siglaUc; }
    public int getAnoLetivo() { return anoLetivo; }
    public double getNotaNormal() { return notaNormal; }
    public double getNotaRecurso() { return notaRecurso; }
    public double getNotaEspecial() { return notaEspecial; }
}