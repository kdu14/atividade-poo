package br.ufal.ic.p2.wepayu.excecoes;

/** Operacao que so vale para comissionado (venda, comissao). */
public class EmpregadoNaoEhComissionadoException extends WePayUException {

    private static final long serialVersionUID = 1L;

    public EmpregadoNaoEhComissionadoException() {
        super("Empregado nao eh comissionado.");
    }
}
