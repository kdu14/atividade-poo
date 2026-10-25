package br.ufal.ic.p2.wepayu.excecoes;

/** Uma data nao existe ou nao esta no formato d/M/aaaa. A mensagem diz qual delas. */
public class DataInvalidaException extends WePayUException {

    private static final long serialVersionUID = 1L;

    public DataInvalidaException(String mensagem) {
        super(mensagem);
    }
}
