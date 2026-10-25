package br.ufal.ic.p2.wepayu.excecoes;

/** A comissao foi informada para quem nao e comissionado, ou faltou para quem e. */
public class TipoNaoAplicavelException extends WePayUException {

    private static final long serialVersionUID = 1L;

    public TipoNaoAplicavelException() {
        super("Tipo nao aplicavel.");
    }
}
