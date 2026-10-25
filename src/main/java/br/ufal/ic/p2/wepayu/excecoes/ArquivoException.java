package br.ufal.ic.p2.wepayu.excecoes;

/** Falha ao ler ou gravar um arquivo (relatorio da folha ou dados persistidos). */
public class ArquivoException extends WePayUException {

    private static final long serialVersionUID = 1L;

    public ArquivoException(String mensagem) {
        super(mensagem);
    }
}
