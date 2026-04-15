package dal;

import model.Estudante;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class EstudanteDAL {
    private static final String FILE_PATH = "data/estudantes.csv";
    private static final String DELIMITER = ";";

    public Estudante obterEstudantePorId(int numMecBusca) {
        File f = new File(FILE_PATH);
        if (!f.exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] d = linha.split(DELIMITER);
                if (d.length >= 7) {
                    try {
                        if (Integer.parseInt(d[0]) == numMecBusca) {
                            return mapearLinhaParaObjeto(d);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    public Estudante obterEstudantePorEmail(String emailBusca) {
        File f = new File(FILE_PATH);
        if (!f.exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] d = linha.split(DELIMITER);
                // O email está na posição 3 (numMec;nif;nome;email;...)
                if (d.length >= 7 && d[3].equalsIgnoreCase(emailBusca)) {
                    return mapearLinhaParaObjeto(d);
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    public boolean adicionarEstudante(Estudante e) {
        File f = new File(FILE_PATH);
        f.getParentFile().mkdirs();

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f, true)))) {
            out.println(e.toCSV());
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public boolean atualizarEstudante(Estudante e) {
        File original = new File(FILE_PATH);
        if (!original.exists()) return false;

        File temp = new File("data/estudantes_temp.csv");
        boolean encontrado = false;

        try (BufferedReader br = new BufferedReader(new FileReader(original));
             PrintWriter pw = new PrintWriter(new FileWriter(temp))) {

            String linha;
            while ((linha = br.readLine()) != null) {
                String[] d = linha.split(DELIMITER);
                if (d.length >= 7) {
                    try {
                        if (Integer.parseInt(d[0]) == e.getNumMec()) {
                            pw.println(e.toCSV());
                            encontrado = true;
                            continue;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
                pw.println(linha);
            }
        } catch (IOException ex) {
            return false;
        }

        if (encontrado) {
            try {
                Files.move(temp.toPath(), original.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return true;
            } catch (IOException ex) {
                return false;
            }
        }

        temp.delete();
        return false;
    }

    private Estudante mapearLinhaParaObjeto(String[] d) {
        return new Estudante(
                Integer.parseInt(d[0]),    // numMec
                d[1],                      // nif
                d[2],                      // nome
                d[3],                      // email
                d[4],                      // morada
                Integer.parseInt(d[5]),    // idCurso
                Double.parseDouble(d[6])   // saldoDevedor
        );
    }

    public int contarTotalEstudantes() {
        int contador = 0;
        java.io.File f = new java.io.File(FILE_PATH);

        if (!f.exists()) return 0;

        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(f))) {
            br.readLine(); // Pular a primeira linha (cabeçalho)
            while (br.readLine() != null) {
                contador++;
            }
        } catch (java.io.IOException e) {
            System.err.println("Erro ao contar estudantes: " + e.getMessage());
            return 0;
        }
        return contador;
    }

    public boolean existeEstudante(int numMecBusca) {
        return obterEstudantePorId(numMecBusca) != null;
    }

    public int contarAlunosNoCurso(String siglaCurso) {
        int contador = 0;
        java.io.File f = new java.io.File(FILE_PATH);

        if (!f.exists()) return 0;

        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(f))) {
            String linha = br.readLine();
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(DELIMITER);

                 if (dados.length >= 8 && dados[7].equalsIgnoreCase(siglaCurso)) {
                    contador++;
                }
            }
        } catch (java.io.IOException e) {
            System.err.println("Erro ao ler o ficheiro de estudantes: " + e.getMessage());
        }
        return contador;
    }
}