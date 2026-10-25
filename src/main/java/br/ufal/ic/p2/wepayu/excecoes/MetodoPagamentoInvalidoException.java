package br.ufal.ic.p2.wepayu.excecoes;

/** Metodo de pagamento diferente de emMaos, correios ou banco. */
public class MetodoPagamentoInvalidoException extends WePayUException {

    private static final long serialVersionUID = 1L;

    public MetodoPagamentoInvalidoException() {
        super("Metodo de pagamento invalido.");
    }
}
