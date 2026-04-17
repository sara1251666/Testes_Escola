package bll;

import common.EmailService;
import common.OperationResult;
import common.PasswordGenerator;
import common.SecurityUtil;
import dal.AutenticacaoDAL;
import dal.DocenteDAL;
import dal.EstudanteDAL;
import model.Docente;
import model.Estudante;
import model.RepositorioSessao;

public class AutenticacaoBLL {
    private final AutenticacaoDAL dal         = new AutenticacaoDAL();
    private final EstudanteDAL    estudanteDAL = new EstudanteDAL();
    private final DocenteDAL      docenteDAL   = new DocenteDAL();

    public OperationResult<String> fazerLogin(String email, String passwordPlain) {
        if (email == null || email.trim().isEmpty() || passwordPlain == null || passwordPlain.isEmpty()) {
            return OperationResult.error("As credenciais não podem estar vazias.");
        }

        String emailLimpo = email.trim().toLowerCase();
       boolean isEmailValido = emailLimpo.endsWith("@issmf.ipp.pt") || emailLimpo.equals("admin@issmf.pt");

        if (!isEmailValido) {
            return OperationResult.error("O e-mail introduzido não tem um domínio válido para o sistema.");
        }

        String[] infoUtilizador = dal.validarCredenciais(emailLimpo, passwordPlain);

        if (infoUtilizador == null) {
            return OperationResult.error("Credenciais inválidas ou conta inexistente.");
        }

        String tipo      = infoUtilizador[0];
        String idOuSigla = infoUtilizador[1];

        if ("ESTUDANTE".equals(tipo)) {
            try {
                int numMec = Integer.parseInt(idOuSigla);
                Estudante e = estudanteDAL.obterPorId(numMec);
                if (e != null && !e.isAtivo()) {
                    return OperationResult.error("Conta desativada. Contacte a secretaria.");
                }
            } catch (NumberFormatException ignored) {}
        } else if ("DOCENTE".equals(tipo)) {
            Docente d = docenteDAL.obterPorSigla(idOuSigla);
            if (d != null && !d.isAtivo()) {
                return OperationResult.error("Conta desativada. Contacte os RH.");
            }
        }

        configurarSessao(tipo, emailLimpo, idOuSigla);

        return OperationResult.success("Login efetuado com sucesso.", tipo);
    }

    /**
     * Método auxiliar para organizar a sessão (Clean Code)
     */
    private void configurarSessao(String tipo, String email, String idOuSigla) {
        RepositorioSessao sessao = RepositorioSessao.getInstance();
        sessao.limparSessao();
        sessao.setTipoUtilizadorLogado(tipo);
        sessao.setEmailUtilizadorLogado(email);

        switch (tipo) {
            case "ESTUDANTE" -> {
                try { sessao.setIdUtilizadorLogado(Integer.parseInt(idOuSigla)); }
                catch (NumberFormatException e) { sessao.setIdUtilizadorLogado(-1); }
            }
            case "DOCENTE" -> {
                sessao.setSiglaDocenteLogado(idOuSigla);
                sessao.setIdUtilizadorLogado(-1);
            }
            case "GESTOR" -> sessao.setIdUtilizadorLogado(1);
        }
    }

    /**
     * Recuperação de password: gera nova password, atualiza no ficheiro e envia por email.
     */
    public OperationResult<Void> recuperarPassword(String email) {
        if (email == null || !email.contains("@")) {
            return OperationResult.error("Formato de e-mail inválido.");
        }
        if (!dal.existeEmail(email.trim())) {
            return OperationResult.error("O e-mail indicado não está registado no sistema.");
        }

        String novaPassPlain = PasswordGenerator.gerarPassword(8);
        String novaPassHash  = SecurityUtil.hashPassword(novaPassPlain);

        if (dal.atualizarPassword(email.trim(), novaPassHash)) {
            EmailService.enviarCredenciais(email.trim(), "Utilizador", email.trim(), novaPassPlain);
            return OperationResult.success(
                    "Uma nova password foi gerada e enviada para o e-mail indicado.", null);
        }

        return OperationResult.error("Erro ao atualizar a password. Tente novamente.");
    }
}