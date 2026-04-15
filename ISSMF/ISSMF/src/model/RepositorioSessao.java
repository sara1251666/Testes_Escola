package model;

public class RepositorioSessao {
    private static RepositorioSessao instancia;

    private int idUtilizadorLogado;
    private String tipoUtilizadorLogado;
    private int anoAtual = 2026;

    private RepositorioSessao() { }

    public static RepositorioSessao getInstance() {
        if (instancia == null) {
            instancia = new RepositorioSessao();
        }
        return instancia;
    }

    public int getIdUtilizadorLogado() { return idUtilizadorLogado; }
    public void setIdUtilizadorLogado(int idUtilizadorLogado) { this.idUtilizadorLogado = idUtilizadorLogado; }

    public String getTipoUtilizadorLogado() { return tipoUtilizadorLogado; }
    public void setTipoUtilizadorLogado(String tipoUtilizadorLogado) { this.tipoUtilizadorLogado = tipoUtilizadorLogado; }

    public int getAnoAtual() { return anoAtual; }
    public void setAnoAtual(int anoAtual) { this.anoAtual = anoAtual; }

    public void limparSessao() {
        this.idUtilizadorLogado = -1;
        this.tipoUtilizadorLogado = null;
    }
}