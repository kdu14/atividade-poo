package br.ufal.ic.p2.wepayu.excecoes;

/** O atributo pedido nao existe (ou nao pode ser usado neste comando). */
public class AtributoInexistenteException extends WePayUException {

    private static final long serialVersionUID = 1L;

    public AtributoInexistenteException() {
        super("Atributo nao existe.");
    }
}
