package br.ufal.ic.p2.wepayu.excecoes;

/** A descricao nao segue o formato "mensal D", "mensal $", "semanal D" ou "semanal N D". */
public class DescricaoDeAgendaInvalidaException extends WePayUException {

    private static final long serialVersionUID = 1L;

    public DescricaoDeAgendaInvalidaException() {
        super("Descricao de agenda invalida");
    }
}
