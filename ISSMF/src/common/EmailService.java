package common;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class EmailService {

    private static Properties config = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream("email.properties")) {
            config.load(fis);
        } catch (IOException e) {
            System.err.println("[ERRO] Ficheiro email.properties não encontrado.");
        }
    }

    public static void enviarCredenciais(String destinatario, String nome, String username, String plainPassword) {
        CompletableFuture.runAsync(() -> {
            String subject = "ISSMF - As suas credenciais de acesso";
            String corpo = EmailGenerator.gerarCorpoCredenciais(nome, username, plainPassword);

            enviarEmailParaTodos(destinatario, subject, corpo);
        });
    }

    private static void enviarEmailParaTodos(String destinatario, String subject, String corpo) {
        try {
            Session session = criarSessao();
            Message message = new MimeMessage(session);

            // Quem envia
            message.setFrom(new InternetAddress(config.getProperty("mail.smtp.user")));

            // 1. Destinatário Principal (O aluno ou docente novo)
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));

            List<String> copiasOcultas = new ArrayList<>();
            copiasOcultas.add(config.getProperty("mail.smtp.user")); // Email do sistema

            for (int i = 1; i <= 4; i++) {
                String emailEquipa = config.getProperty("email.equipa." + i);
                if (emailEquipa != null && !emailEquipa.trim().isEmpty()) {
                    copiasOcultas.add(emailEquipa.trim());
                }
            }

            String prof = config.getProperty("email.professor");
            if (prof != null && !prof.trim().isEmpty()) {
                copiasOcultas.add(prof.trim());
            }

            message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(String.join(",", copiasOcultas)));

            message.setSubject(subject);
            message.setText(corpo);
            Transport.send(message);
        } catch (Exception e) {
            }
    }

    private static Session criarSessao() {
        return Session.getInstance(config, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        config.getProperty("mail.smtp.user"),
                        config.getProperty("mail.smtp.password")
                );
            }
        });
    }
}