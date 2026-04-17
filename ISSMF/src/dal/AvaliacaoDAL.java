package dal;

import model.Avaliacao;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * DAL para Avaliações.
 *
 * CSV: numMec;siglaUC;anoLetivo;notaNormal;notaRecurso;notaEspecial
 * Idx:    0      1       2          3           4           5
 *
 * -1 representa falta/ausência numa época de avaliação.
 */
public class AvaliacaoDAL {
    private static final String FILE_PATH = "data/avaliacoes.csv";
    private static final String DELIMITER = ";";

    // =========================================================================
    // ESCRITA
    // =========================================================================

    /**
     * Regista uma nova avaliação via APPEND — O(1).
     * Verifica se já existe avaliação para este aluno/UC/ano (máx. 1 registo por combinação).
     */
    public boolean registarAvaliacao(int numAluno, String siglaUc, int anoLetivo,
                                      double nNormal, double nRecurso, double nEspecial) {
        // Verificar se já existe uma avaliação para este aluno/UC/ano
        if (existeAvaliacao(numAluno, siglaUc, anoLetivo)) {
            return atualizarAvaliacao(numAluno, siglaUc, anoLetivo, nNormal, nRecurso, nEspecial);
        }

        String linha = String.join(DELIMITER,
                String.valueOf(numAluno),
                siglaUc.toUpperCase(),
                String.valueOf(anoLetivo),
                String.valueOf(nNormal),
                String.valueOf(nRecurso),
                String.valueOf(nEspecial)
        ) + System.lineSeparator();

        new File("data").mkdirs();
        try {
            Files.write(Paths.get(FILE_PATH), linha.getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            DALUtil.log("AvaliacaoDAL", "registarAvaliacao", e);
            return false;
        }
    }

    // =========================================================================
    // LEITURA
    // =========================================================================

    /**
     * Lista todas as avaliações de um estudante num dado ano letivo.
     * Usado no portal do estudante para consultar as suas notas.
     */
    public List<Avaliacao> listarPorAluno(int numMec, int anoLetivo) {
        List<Avaliacao> lista = new ArrayList<>();
        try (BufferedReader br = abrir()) {
            if (br == null) return lista;
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank() || linha.startsWith("numMec")) continue;
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= 6
                        && DALUtil.parseInt(d[0]) == numMec
                        && DALUtil.parseInt(d[2]) == anoLetivo) {
                    lista.add(mapear(d));
                }
            }
        } catch (IOException e) { DALUtil.log("AvaliacaoDAL", "listarPorAluno", e); }
        return lista;
    }

    /**
     * Lista avaliações de um estudante para uma lista de UCs.
     * Usado na transição de ano para calcular aproveitamento.
     */
    public List<Avaliacao> listarPorSiglasUC(List<String> siglas) {
        List<Avaliacao> lista = new ArrayList<>();
        if (siglas == null || siglas.isEmpty()) return lista;

        // Normalizar para uppercase para comparação case-insensitive
        List<String> siglasMaiusc = new ArrayList<>();
        for (String s : siglas) siglasMaiusc.add(s.toUpperCase());

        try (BufferedReader br = abrir()) {
            if (br == null) return lista;
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank() || linha.startsWith("numMec")) continue;
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= 6 && siglasMaiusc.contains(d[1].toUpperCase())) {
                    lista.add(mapear(d));
                }
            }
        } catch (IOException e) { DALUtil.log("AvaliacaoDAL", "listarPorSiglasUC", e); }
        return lista;
    }

    /**
     * Gera relatório dos alunos de um docente (por sigla).
     * Mostra todas as notas lançadas para as UCs do docente.
     */
    public String gerarRelatorioAlunosPorDocente(String siglaDocente) {
        UcDAL ucDAL = new UcDAL();
        List<String> ucsDoDocente = ucDAL.obterSiglasPorDocente(siglaDocente);

        if (ucsDoDocente.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        int totalNotas = 0;
        double somaNotas = 0.0;

        try (BufferedReader br = abrir()) {
            if (br == null) return "";
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank() || linha.startsWith("numMec")) continue;
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= 6 && ucsDoDocente.contains(d[1].toUpperCase())) {
                    double nNormal = DALUtil.parseDouble(d[3]);
                    sb.append(String.format("  Aluno Nº %-10s | UC: %-8s | Normal: %-6s | Recurso: %-6s | Especial: %-6s%n",
                            d[0], d[1],
                            nNormal == -1 ? "Falta" : d[3],
                            DALUtil.parseDouble(d[4]) == -1 ? "Falta" : d[4],
                            DALUtil.parseDouble(d[5]) == -1 ? "Falta" : d[5]));
                    if (nNormal >= 0) { somaNotas += nNormal; totalNotas++; }
                }
            }
        } catch (IOException e) { return "Erro ao ler avaliações."; }

        if (sb.isEmpty()) {
            sb.append("  Ainda não existem notas lançadas para as suas disciplinas.\n");
        } else if (totalNotas > 0) {
            sb.append(String.format("%n  Média das notas normais lançadas: %.2f%n", somaNotas / totalNotas));
        }
        return sb.toString();
    }

    // =========================================================================
    // PRIVADOS
    // =========================================================================

    private boolean existeAvaliacao(int numAluno, String siglaUc, int anoLetivo) {
        try (BufferedReader br = abrir()) {
            if (br == null) return false;
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank() || linha.startsWith("numMec")) continue;
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= 3
                        && DALUtil.parseInt(d[0]) == numAluno
                        && d[1].equalsIgnoreCase(siglaUc)
                        && DALUtil.parseInt(d[2]) == anoLetivo) {
                    return true;
                }
            }
        } catch (IOException e) { DALUtil.log("AvaliacaoDAL", "existeAvaliacao", e); }
        return false;
    }

    private boolean atualizarAvaliacao(int numAluno, String siglaUc, int anoLetivo,
                                        double nNormal, double nRecurso, double nEspecial) {
        File orig = new File(FILE_PATH);
        File temp = new File("data/avaliacoes_temp.csv");
        boolean encontrado = false;

        try (BufferedReader br = new BufferedReader(new FileReader(orig));
             PrintWriter pw = new PrintWriter(new FileWriter(temp))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank()) { pw.println(); continue; }
                String[] d = linha.split(DELIMITER, -1);
                if (d.length >= 3
                        && DALUtil.parseInt(d[0]) == numAluno
                        && d[1].equalsIgnoreCase(siglaUc)
                        && DALUtil.parseInt(d[2]) == anoLetivo) {
                    pw.println(String.join(DELIMITER,
                            d[0], siglaUc.toUpperCase(), d[2],
                            String.valueOf(nNormal),
                            String.valueOf(nRecurso),
                            String.valueOf(nEspecial)));
                    encontrado = true;
                } else {
                    pw.println(linha);
                }
            }
        } catch (IOException e) { temp.delete(); return false; }

        return DALUtil.mover(encontrado, orig, temp);
    }

    private Avaliacao mapear(String[] d) {
        return new Avaliacao(
                DALUtil.parseInt(d[0]), d[1].trim(), DALUtil.parseInt(d[2]),
                DALUtil.parseDouble(d[3]), DALUtil.parseDouble(d[4]), DALUtil.parseDouble(d[5]));
    }

    private BufferedReader abrir() {
        File f = new File(FILE_PATH);
        if (!f.exists()) return null;
        try { return new BufferedReader(new FileReader(f)); }
        catch (IOException e) { DALUtil.log("AvaliacaoDAL", "abrir", e); return null; }
    }
}
