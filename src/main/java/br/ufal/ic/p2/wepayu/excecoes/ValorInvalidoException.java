package br.ufal.ic.p2.wepayu.excecoes;

/** Um valor informado nao e aceito: nao numerico, negativo, fora da faixa... A mensagem diz qual. */
public class ValorInvalidoException extends WePayUException {

    private static final long serialVersionUID = 1L;

    public ValorInvalidoException(String mensagem) {
        super(mensagem);
    }
}
