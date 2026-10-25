package br.ufal.ic.p2.wepayu.excecoes;

/** A agenda escolhida para o empregado nao esta entre as disponiveis no sistema. */
public class AgendaIndisponivelException extends WePayUException {

    private static final long serialVersionUID = 1L;

    public AgendaIndisponivelException() {
        super("Agenda de pagamento nao esta disponivel");
    }
}
