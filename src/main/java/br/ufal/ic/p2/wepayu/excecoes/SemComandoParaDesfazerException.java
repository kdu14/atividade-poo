package br.ufal.ic.p2.wepayu.excecoes;

/** Undo pedido sem nenhum comando no historico. */
public class SemComandoParaDesfazerException extends WePayUException {

    private static final long serialVersionUID = 1L;

    public SemComandoParaDesfazerException() {
        super("Nao ha comando a desfazer.");
    }
}
