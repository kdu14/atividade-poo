package br.ufal.ic.p2.wepayu.excecoes;

/**
 * Mae de todas as excecoes de negocio do WePayU. Quem chama pode tratar
 * "qualquer erro do sistema" pegando esta classe, ou um caso especifico
 * pegando a subclasse (empregado nao existe, data invalida, ...).
 */
public abstract class WePayUException extends Exception {

    private static final long serialVersionUID = 1L;

    protected WePayUException(String mensagem) {
        super(mensagem);
    }
}
