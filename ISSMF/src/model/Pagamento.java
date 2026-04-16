package model;

/**
 * Registo de um pagamento de propina (parcial ou total).
 *
 * CSV: numMec;anoLetivo;valor;data
 * Idx:   0       1        2    3
 * O histórico completo de pagamentos de um estudante é a soma de todas
 * as linhas com o seu numMec no mesmo anoLetivo.
 */
public class Pagamento {

    private final int    numMec;
    private final int    anoLetivo;
    private final double valor;
    private final String data;      // formato dd-MM-yyyy

    public Pagamento(int numMec, int anoLetivo, double valor, String data) {
        this.numMec    = numMec;
        this.anoLetivo = anoLetivo;
        this.valor     = valor;
        this.data      = data;
    }

    public int    getNumMec()    { return numMec;    }
    public int    getAnoLetivo() { return anoLetivo; }
    public double getValor()     { return valor;     }
    public String getData()      { return data;      }

    /** numMec;anoLetivo;valor;data */
    public String toCSV() {
        return numMec + ";" + anoLetivo + ";" + valor + ";" + data;
    }

    @Override
    public String toString() {
        return String.format("  %s — %.2f€", data, valor);
    }
}