package common;

/**
 * Responsável por gerar os templates de texto para os emails do sistema.
 */
public class EmailGenerator {

    /**
     * Gera uma mensagem formal de boas-vindas com as credenciais de acesso.
     * Inclui um aviso explícito de segurança para a alteração da password.
     */
    public static String gerarCorpoCredenciais(String nome, String username, String password) {
        return "Assunto: ISSMF - As suas credenciais de acesso ao sistema\n\n" +
                "Estimado(a) " + nome + ",\n\n" +
                "A sua conta no sistema ISSMF (Instituto Superior de Santa Maria da Feira) foi criada com sucesso.\n\n" +
                "Para aceder à sua área pessoal, utilize os seguintes dados:\n" +
                "--------------------------------------------------\n" +
                "  Utilizador: " + username + "\n" +
                "  Password Temporária: " + password + "\n" +
                "--------------------------------------------------\n\n" +
                "AVISO DE SEGURANÇA OBRIGATÓRIO:\n" +
                "Por questões de proteção de dados e segurança da sua conta, " +
                "é obrigatório proceder à alteração desta password no seu primeiro acesso ao sistema.\n\n" +
                "Se não solicitou esta conta ou se tiver alguma dúvida, por favor contacte o suporte técnico.\n\n" +
                "Com os melhores cumprimentos,\n" +
                "A Direção do ISSMF";
    }
}