package br.ufal.ic.p2.wepayu.modelos.agenda;

import java.time.LocalDate;

/** Paga no mesmo dia (1 a 28) de todo mes -- ex.: "mensal 7". */
public class AgendaMensalDiaFixo extends AgendaMensal {

    private static final long serialVersionUID = 1L;

    public static final int ULTIMO_DIA_PERMITIDO = 28;

    private final int dia;

    public AgendaMensalDiaFixo(String descricao, int dia) {
        super(descricao);
        if (dia < 1 || dia > ULTIMO_DIA_PERMITIDO) {
            throw new IllegalArgumentException("Dia da agenda mensal deve ser de 1 a 28.");
        }
        this.dia = dia;
    }

    @Override
    protected LocalDate diaDePagamentoNoMes(LocalDate qualquerDiaDoMes) {
        return qualquerDiaDoMes.withDayOfMonth(dia);
    }
}
