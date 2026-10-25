package br.ufal.ic.p2.wepayu.excecoes;

/** Ja existe uma agenda disponivel com a mesma descricao. */
public class AgendaJaExisteException extends WePayUException {

    private static final long serialVersionUID = 1L;

    public AgendaJaExisteException() {
        super("Agenda de pagamentos ja existe");
    }
}
