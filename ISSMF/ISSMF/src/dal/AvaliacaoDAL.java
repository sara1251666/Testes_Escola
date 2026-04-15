package dal;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class AvaliacaoDAL {
    private static final String FILE_PATH = "data/avaliacoes.csv";
    private static final String DELIMITER = ";";

    /**
     * Adiciona uma nova avaliação ao ficheiro CSV sem o carregar para a memória (O(1) tempo de escrita).
     */
    public boolean registarAvaliacao(int numAluno, String siglaUc, int anoLetivo, double nNormal, double nRecurso, double nEspecial) {

        String linha = String.join(DELIMITER,
                String.valueOf(numAluno),
                siglaUc.toUpperCase(),
                String.valueOf(anoLetivo),
                String.valueOf(nNormal),
                String.valueOf(nRecurso),
                String.valueOf(nEspecial)
        ) + System.lineSeparator();

        File dir = new File("data");
        if (!dir.exists()) dir.mkdir();

        try {
            Files.write(Paths.get(FILE_PATH), linha.getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Lazy Loading O(N): Lê as avaliações linha a linha e junta dados para os alunos do docente.
     */
    public String gerarRelatorioAlunosPorDocente(int idDocente) {
       StringBuilder sb = new StringBuilder();
        int totalNotas = 0;
        double somaNotas = 0.0;

        UcDAL ucDAL = new UcDAL();
        java.util.List<String> ucsDoDocente = ucDAL.obterSiglasPorDocente(idDocente);

        if (ucsDoDocente.isEmpty()) return "";

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(DELIMITER);
                if (dados.length >= 6) {
                    String numAluno = dados[0];
                    String siglaUc = dados[1];
                    double nNormal = Double.parseDouble(dados[3]);

                    if (ucsDoDocente.contains(siglaUc)) {
                        sb.append(">> Aluno Nº: ").append(numAluno)
                                .append(" | UC: ").append(siglaUc)
                                .append(" | Nota Normal: ").append(nNormal == -1 ? "Faltou" : nNormal)
                                .append("\n");

                        if (nNormal != -1) {
                            somaNotas += nNormal;
                            totalNotas++;
                        }
                    }
                }
            }
        } catch (IOException e) {
            return "Erro ao ler ficheiro de avaliações.";
        }

        if (totalNotas > 0) {
            double media = somaNotas / totalNotas;
            sb.append("\n>> Média das notas lançadas: ").append(String.format("%.2f", media)).append("\n");
        } else {
            sb.append("\n>> Ainda não existem notas lançadas válidas para as suas disciplinas.\n");
        }

        return sb.toString();
    }


}