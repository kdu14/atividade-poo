package br.ufal.ic.p2.wepayu.modelos;

import br.ufal.ic.p2.wepayu.util.Exigir;

/** Deposito em conta corrente. */
public class EmBanco extends MetodoPagamento {

    private static final long serialVersionUID = 1L;

    private final String banco;
    private final String agencia;
    private final String contaCorrente;

    public EmBanco(String banco, String agencia, String contaCorrente) {
        this.banco = Exigir.preenchido(banco, "Banco");
        this.agencia = Exigir.preenchido(agencia, "Agencia");
        this.contaCorrente = Exigir.preenchido(contaCorrente, "Conta corrente");
    }

    @Override
    public String getNome() {
        return "banco";
    }

    @Override
    public String getDescricao(String enderecoEmpregado) {
        return banco + ", Ag. " + agencia + " CC " + contaCorrente;
    }

    @Override
    public boolean recebeEmBanco() {
        return true;
    }

    @Override
    public String getBanco() {
        return banco;
    }

    @Override
    public String getAgencia() {
        return agencia;
    }

    @Override
    public String getContaCorrente() {
        return contaCorrente;
    }
}
