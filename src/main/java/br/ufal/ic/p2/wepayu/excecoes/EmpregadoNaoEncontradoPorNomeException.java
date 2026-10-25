package br.ufal.ic.p2.wepayu.excecoes;

/** A busca por nome nao achou empregado na posicao pedida. */
public class EmpregadoNaoEncontradoPorNomeException extends WePayUException {

    private static final long serialVersionUID = 1L;

    public EmpregadoNaoEncontradoPorNomeException() {
        super("Nao ha empregado com esse nome.");
    }
}
