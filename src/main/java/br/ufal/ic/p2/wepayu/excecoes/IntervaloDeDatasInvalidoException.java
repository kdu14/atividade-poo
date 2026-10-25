package br.ufal.ic.p2.wepayu.excecoes;

/** A data inicial de uma consulta por periodo vem depois da final. */
public class IntervaloDeDatasInvalidoException extends WePayUException {

    private static final long serialVersionUID = 1L;

    public IntervaloDeDatasInvalidoException() {
        super("Data inicial nao pode ser posterior aa data final.");
    }
}
