package br.ufal.ic.p2.wepayu.excecoes;

/** Nenhum empregado tem a identificacao informada. */
public class EmpregadoNaoExisteException extends WePayUException {

    private static final long serialVersionUID = 1L;

    public EmpregadoNaoExisteException() {
        super("Empregado nao existe.");
    }
}
