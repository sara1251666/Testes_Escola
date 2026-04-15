package bll;

import common.OperationResult;
import dal.AutenticacaoDAL;
import model.RepositorioSessao;
import common.EmailService;
import common.PasswordGenerator;
import common.SecurityUtil;

public class AutenticacaoBLL {
    private final AutenticacaoDAL dal = new AutenticacaoDAL();

    public OperationResult<String> fazerLogin(String email, String passwordPlain) {
        if (email == null || email.trim().isEmpty() || passwordPlain == null || passwordPlain.isEmpty()) {
            return OperationResult.error("As credenciais não podem estar vazias.");
        }

        String hashDigitado = SecurityUtil.hashPassword(passwordPlain);
        String[] infoUtilizador = dal.validarCredenciais(email, hashDigitado);

        if (infoUtilizador != null) {
            String tipoPerfil = infoUtilizador[0];
            int idInterno = Integer.parseInt(infoUtilizador[1]);

            RepositorioSessao sessao = RepositorioSessao.getInstance();
            sessao.limparSessao();
            sessao.setTipoUtilizadorLogado(tipoPerfil);
            sessao.setIdUtilizadorLogado(idInterno);

            return OperationResult.success("Login efetuado com sucesso.", tipoPerfil);
        }

        return OperationResult.error("Credenciais inválidas ou conta inexistente.");
    }

    public OperationResult<Void> recuperarPassword(String email) {
        if (email == null || !email.contains("@")) {
            return OperationResult.error("Formato de e-mail inválido.");
        }

        if (!dal.existeEmail(email)) {
            return OperationResult.error("O e-mail indicado não está registado no sistema.");
        }

        String novaPasswordPlain = common.PasswordGenerator.gerarPassword(8);
        String novaPasswordHash = common.SecurityUtil.hashPassword(novaPasswordPlain);

        boolean atualizado = dal.atualizarPassword(email, novaPasswordHash);

        if (atualizado) {
            EmailService.enviarCredenciais(email, "Utilizador", email, novaPasswordPlain);

            return OperationResult.success("Uma nova password foi gerada e enviada para o seu e-mail.", null);
        }

        return OperationResult.error("Ocorreu um erro ao tentar gravar a nova password no sistema de ficheiros.");
    }
}