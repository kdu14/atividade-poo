package br.ufal.ic.p2.wepayu.modelos;

public class EmBanco extends MetodoPagamento {

    private final String banco;
    private final String agencia;
    private final String contaCorrente;

    public EmBanco(String banco, String agencia, String contaCorrente) {
        this.banco = banco;
        this.agencia = agencia;
        this.contaCorrente = contaCorrente;
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
