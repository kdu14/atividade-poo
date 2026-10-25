package br.ufal.ic.p2.wepayu.excecoes;

/** Operacao que so vale para membro do sindicato. */
public class EmpregadoNaoSindicalizadoException extends WePayUException {

    private static final long serialVersionUID = 1L;

    public EmpregadoNaoSindicalizadoException() {
        super("Empregado nao eh sindicalizado.");
    }
}
