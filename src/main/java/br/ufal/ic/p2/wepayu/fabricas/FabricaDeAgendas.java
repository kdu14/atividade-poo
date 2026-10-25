package br.ufal.ic.p2.wepayu.fabricas;

import br.ufal.ic.p2.wepayu.modelos.TipoEmpregado;
import br.ufal.ic.p2.wepayu.modelos.agenda.AgendaMensalUltimoDiaUtil;
import br.ufal.ic.p2.wepayu.modelos.agenda.AgendaPagamento;
import br.ufal.ic.p2.wepayu.modelos.agenda.AgendaSemanal;

import java.time.DayOfWeek;
import java.util.List;

/** Cria as agendas de pagamento. */
public final class FabricaDeAgendas {

    private FabricaDeAgendas() {
    }

    /** A agenda com que cada tipo de empregado e criado (descricao geral do enunciado). */
    public static AgendaPagamento padraoDe(TipoEmpregado tipo) {
        return switch (tipo) {
            case HORISTA -> new AgendaSemanal("semanal 5", 1, DayOfWeek.FRIDAY);
            case ASSALARIADO -> new AgendaMensalUltimoDiaUtil("mensal $");
            case COMISSIONADO -> new AgendaSemanal("semanal 2 5", 2, DayOfWeek.FRIDAY);
        };
    }

    /** As agendas disponiveis num sistema recem-criado: as tres padrao. */
    public static List<AgendaPagamento> padroes() {
        return List.of(padraoDe(TipoEmpregado.HORISTA), padraoDe(TipoEmpregado.ASSALARIADO),
                padraoDe(TipoEmpregado.COMISSIONADO));
    }
}
