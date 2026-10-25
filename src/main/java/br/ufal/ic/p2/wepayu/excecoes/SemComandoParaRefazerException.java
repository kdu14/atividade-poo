package br.ufal.ic.p2.wepayu.excecoes;

/** Redo pedido sem nenhum comando desfeito. */
public class SemComandoParaRefazerException extends WePayUException {

    private static final long serialVersionUID = 1L;

    public SemComandoParaRefazerException() {
        super("Nao ha comando a refazer.");
    }
}
