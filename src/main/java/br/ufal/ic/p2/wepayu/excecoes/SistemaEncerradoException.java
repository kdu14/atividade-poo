package br.ufal.ic.p2.wepayu.excecoes;

/** Comando recebido depois de encerrarSistema. */
public class SistemaEncerradoException extends WePayUException {

    private static final long serialVersionUID = 1L;

    public SistemaEncerradoException() {
        super("Nao pode dar comandos depois de encerrarSistema.");
    }
}
