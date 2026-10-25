package br.ufal.ic.p2.wepayu.fabricas;

import br.ufal.ic.p2.wepayu.excecoes.DescricaoDeAgendaInvalidaException;
import br.ufal.ic.p2.wepayu.modelos.TipoEmpregado;
import br.ufal.ic.p2.wepayu.modelos.agenda.AgendaMensalDiaFixo;
import br.ufal.ic.p2.wepayu.modelos.agenda.AgendaMensalUltimoDiaUtil;
import br.ufal.ic.p2.wepayu.modelos.agenda.AgendaPagamento;
import br.ufal.ic.p2.wepayu.modelos.agenda.AgendaSemanal;

import java.time.DayOfWeek;
import java.util.List;

/**
 * Cria as agendas de pagamento, a partir do tipo do empregado (as padrao)
 * ou da descricao usada nos comandos. Formatos aceitos:
 * <ul>
 *   <li>"mensal D": dia D (1 a 28) de todo mes;</li>
 *   <li>"mensal $": ultimo dia util de todo mes;</li>
 *   <li>"semanal D": toda semana, no dia D (1 = segunda ... 7 = domingo);</li>
 *   <li>"semanal N D": a cada N semanas (1 a 52), no dia D.</li>
 * </ul>
 */
public final class FabricaDeAgendas {

    private static final String MENSAL = "mensal";
    private static final String SEMANAL = "semanal";
    private static final String ULTIMO_DIA_UTIL = "$";
    private static final int SEMANAS_NO_ANO = 52;

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

    /** A agenda descrita pelo texto; a descricao guardada e o texto com espacos normalizados. */
    public static AgendaPagamento aPartirDe(String descricao) throws DescricaoDeAgendaInvalidaException {
        if (descricao == null || descricao.isBlank()) {
            throw new DescricaoDeAgendaInvalidaException();
        }
        String[] partes = descricao.trim().split("\\s+");
        String normalizada = String.join(" ", partes);

        if (partes[0].equals(MENSAL) && partes.length == 2) {
            return mensal(normalizada, partes[1]);
        }
        if (partes[0].equals(SEMANAL) && partes.length == 2) {
            return new AgendaSemanal(normalizada, 1, diaDaSemana(partes[1]));
        }
        if (partes[0].equals(SEMANAL) && partes.length == 3) {
            int intervalo = numeroEntre(partes[1], 1, SEMANAS_NO_ANO);
            return new AgendaSemanal(normalizada, intervalo, diaDaSemana(partes[2]));
        }
        throw new DescricaoDeAgendaInvalidaException();
    }

    private static AgendaPagamento mensal(String descricao, String dia) throws DescricaoDeAgendaInvalidaException {
        if (dia.equals(ULTIMO_DIA_UTIL)) {
            return new AgendaMensalUltimoDiaUtil(descricao);
        }
        return new AgendaMensalDiaFixo(descricao, numeroEntre(dia, 1, AgendaMensalDiaFixo.ULTIMO_DIA_PERMITIDO));
    }

    private static DayOfWeek diaDaSemana(String texto) throws DescricaoDeAgendaInvalidaException {
        return DayOfWeek.of(numeroEntre(texto, 1, 7));
    }

    private static int numeroEntre(String texto, int minimo, int maximo) throws DescricaoDeAgendaInvalidaException {
        try {
            int numero = Integer.parseInt(texto);
            if (numero >= minimo && numero <= maximo) {
                return numero;
            }
        } catch (NumberFormatException e) {
            // cai no erro de negocio abaixo
        }
        throw new DescricaoDeAgendaInvalidaException();
    }
}
