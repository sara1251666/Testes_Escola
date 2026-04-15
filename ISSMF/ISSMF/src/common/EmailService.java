package common;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class EmailService {
    private static final String SMTP_HOST = "smtp.escola.pt";
    private static final String SMTP_PORT = "587";
    private static final String USERNAME = "noreply@escola.pt";
    private static final String PASSWORD = "passwordSegura123";
    private static final String GESTOR_EMAIL = "admin@escola.pt";

    public static void enviarCredenciais(String destinatario, String nome, String username, String plainPassword) {
        CompletableFuture.runAsync(() -> {
            try {
                String targetEmail = destinatario;

                if (destinatario.endsWith("@escola.pt") && !destinatario.contains("real")) {
                    targetEmail = GESTOR_EMAIL;
                    System.out.println("[INFO Sistema] Email redirecionado para o Gestor (admin).");
                }

                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", SMTP_HOST);
                props.put("mail.smtp.port", SMTP_PORT);

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(USERNAME, PASSWORD);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(USERNAME));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(targetEmail));
                message.setSubject("ISSMF - As suas credenciais de acesso");

                String corpo = String.format(
                        "Olá %s,\n\nA sua conta foi criada com sucesso.\nUtilizador: %s\nPassword: %s\n\nPor favor, altere a password no primeiro login.",
                        nome, username, plainPassword
                );

                message.setText(corpo);

                System.out.println("\n[MOCK EMAIL SENT] Para: " + targetEmail + " | Assunto: Credenciais enviadas!");

            } catch (Exception e) {
                System.err.println("[ERRO EMAIL] Falha ao enviar: " + e.getMessage());
            }
        });
    }
}