package br.ufal.ic.p2.wepayu.excecoes;

/** Operacao que so vale para horista (cartao de ponto, horas trabalhadas). */
public class EmpregadoNaoEhHoristaException extends WePayUException {

    private static final long serialVersionUID = 1L;

    public EmpregadoNaoEhHoristaException() {
        super("Empregado nao eh horista.");
    }
}
