package br.ufal.ic.p2.wepayu.modelos.agenda;

import br.ufal.ic.p2.wepayu.util.Datas;

import java.time.LocalDate;

/** Paga no ultimo dia util de todo mes, desconsiderando feriados -- "mensal $". */
public class AgendaMensalUltimoDiaUtil extends AgendaMensal {

    private static final long serialVersionUID = 1L;

    public AgendaMensalUltimoDiaUtil(String descricao) {
        super(descricao);
    }

    @Override
    protected LocalDate diaDePagamentoNoMes(LocalDate qualquerDiaDoMes) {
        return Datas.ultimoDiaUtilDoMes(qualquerDiaDoMes);
    }
}
