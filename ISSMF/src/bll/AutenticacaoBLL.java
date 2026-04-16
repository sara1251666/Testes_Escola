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

    /**
     * Autentica um utilizador.
     * A verificação de password usa SecurityUtil.verificarPassword() com o
     * salt já armazenado — NUNCA gera um novo hash antes de comparar.
     *
     * @return OperationResult com o tipo de perfil ("ESTUDANTE","DOCENTE","GESTOR").
     */
    public OperationResult<String> fazerLogin(String email, String passwordPlain) {
        if (email == null || email.trim().isEmpty()
                || passwordPlain == null || passwordPlain.isEmpty()) {
            return OperationResult.error("As credenciais não podem estar vazias.");
        }

        // validarCredenciais já chama SecurityUtil.verificarPassword internamente
        String[] infoUtilizador = dal.validarCredenciais(email.trim(), passwordPlain);

        if (infoUtilizador != null) {
            String tipo      = infoUtilizador[0];   // ESTUDANTE | DOCENTE | GESTOR
            String idOuSigla = infoUtilizador[1];   // numMec | sigla | "1"

            // Verificar se o utilizador está ativo (ESTUDANTE e DOCENTE podem ser desativados)
            if ("ESTUDANTE".equals(tipo)) {
                try {
                    int numMec = Integer.parseInt(idOuSigla);
                    Estudante e = estudanteDAL.obterPorId(numMec);
                    if (e != null && !e.isAtivo()) {
                        return OperationResult.error(
                                "Conta desativada. Contacte a secretaria do ISSMF.");
                    }
                } catch (NumberFormatException ignored) {}
            } else if ("DOCENTE".equals(tipo)) {
                Docente d = docenteDAL.obterPorSigla(idOuSigla);
                if (d != null && !d.isAtivo()) {
                    return OperationResult.error(
                            "Conta desativada. Contacte os serviços de RH do ISSMF.");
                }
            }

            RepositorioSessao sessao = RepositorioSessao.getInstance();
            sessao.limparSessao();
            sessao.setTipoUtilizadorLogado(tipo);
            sessao.setEmailUtilizadorLogado(email.trim());

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

            return OperationResult.success("Login efetuado com sucesso.", tipo);
        }

        return OperationResult.error("Credenciais inválidas ou conta inexistente.");
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
            // Envia por email sem mostrar na consola
            EmailService.enviarCredenciais(email.trim(), "Utilizador", email.trim(), novaPassPlain);
            return OperationResult.success(
                    "Uma nova password foi gerada e enviada para o e-mail indicado.", null);
        }

        return OperationResult.error("Erro ao atualizar a password. Tente novamente.");
    }
}