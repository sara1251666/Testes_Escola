package bll;

import common.EmailService;
import common.OperationResult;
import common.SecurityUtil;
import dal.AutenticacaoDAL;
import dal.EstudanteDAL;
import model.Estudante;

import java.util.UUID;

public class EstudanteBLL {

    // 1. Variáveis de Acesso a Dados devidamente declaradas
    private final EstudanteDAL estudanteDAL;
    private final AutenticacaoDAL authDAL;

    public EstudanteBLL() {
        this.estudanteDAL = new EstudanteDAL();
        this.authDAL = new AutenticacaoDAL();
    }

    // --- REGRA: Auto-Matrícula ---
    public OperationResult<Estudante> registarAutoMatricula(String nome, String nif, String email, int idCurso) {
        // Verifica duplicados (usando o email como critério de verificação)
        if (estudanteDAL.obterEstudantePorEmail(email) != null) {
            return OperationResult.error("Já existe um estudante registado com este Email.");
        }

        // Usa o construtor de Auto-Matrícula que criámos na classe Estudante
        Estudante estudante = new Estudante(nif, nome, email, idCurso);

        // Gerar Password e fazer Hash
        String passwordGerada = UUID.randomUUID().toString().substring(0, 8);
        String hash = SecurityUtil.hashPassword(passwordGerada);

        // Gravar Estudante
        boolean sucesso = estudanteDAL.adicionarEstudante(estudante);

        if (sucesso) {
            // Idealmente a authDAL deve registar as credenciais novas
            // authDAL.registarNovaCredencial(estudante.getNumMec(), hash, "ESTUDANTE");

            // Enviar email
            EmailService.enviarCredenciais(email, nome, String.valueOf(estudante.getNumMec()), passwordGerada);

            return OperationResult.success("Matrícula realizada com sucesso. As credenciais foram enviadas por email.", estudante);
        }

        return OperationResult.error("Erro de sistema ao gravar os dados da matrícula.");
    }

    // --- REGRA: Obter Aluno Logado ---
    public Estudante obterEstudante(int idLogado) {
        return estudanteDAL.obterEstudantePorId(idLogado);
    }

    // --- REGRA: Atualizar Morada ---
    public OperationResult<Estudante> atualizarMorada(Estudante estudante, String novaMorada) {
        estudante.setMorada(novaMorada);
        boolean sucesso = estudanteDAL.atualizarEstudante(estudante);

        if (sucesso) {
            return OperationResult.success("Morada atualizada com sucesso.", estudante);
        }
        return OperationResult.error("Falha ao gravar a nova morada no ficheiro.");
    }

    // --- REGRA: Alterar Password ---
    public OperationResult<Estudante> alterarPassword(Estudante estudante, String novaPassPlain) {
        if (novaPassPlain.length() < 6) {
            return OperationResult.error("A password deve ter no mínimo 6 caracteres.");
        }

        String hash = SecurityUtil.hashPassword(novaPassPlain);
        boolean sucesso = authDAL.atualizarPasswordPorId(estudante.getNumMec(), hash);

        if (sucesso) {
            return OperationResult.success("Password atualizada com segurança.", estudante);
        }
        return OperationResult.error("Erro ao atualizar password na base de dados.");
    }

    public OperationResult<Estudante> pagarPropinas(Estudante estudante) {
        estudante.setSaldoDevedor(0.0); // Zera a dívida
        boolean sucesso = estudanteDAL.atualizarEstudante(estudante);

        if (sucesso) {
            return OperationResult.success("Pagamento registado com sucesso! Saldo regularizado.", estudante);
        }
        return OperationResult.error("Ocorreu um erro ao processar o pagamento.");
    }
}