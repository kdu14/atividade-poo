package br.ufal.ic.p2.wepayu.modelos.agenda;

import br.ufal.ic.p2.wepayu.util.Datas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

/**
 * Paga num dia da semana, a cada N semanas. O primeiro pagamento e a N-esima
 * ocorrencia desse dia a partir de 1/1/2005 -- por isso "semanal 2 5" paga
 * primeiro em 14/1/2005 (a 2a sexta), como o comissionado do Milestone 1.
 */
public class AgendaSemanal extends AgendaPagamento {

    private static final long serialVersionUID = 1L;

    /** O ano tem 52 semanas: cada semana vale salario*12/52 (ver US7). */
    private static final BigDecimal MESES_NO_ANO = BigDecimal.valueOf(12);
    private static final BigDecimal SEMANAS_NO_ANO = BigDecimal.valueOf(52);

    private final int intervaloEmSemanas;
    private final DayOfWeek diaDaSemana;

    public AgendaSemanal(String descricao, int intervaloEmSemanas, DayOfWeek diaDaSemana) {
        super(descricao);
        if (intervaloEmSemanas < 1) {
            throw new IllegalArgumentException("Intervalo da agenda deve ser de pelo menos 1 semana.");
        }
        this.intervaloEmSemanas = intervaloEmSemanas;
        this.diaDaSemana = diaDaSemana;
    }

    @Override
    public boolean ehDiaDePagamento(LocalDate data) {
        LocalDate primeiro = primeiroPagamento();
        return data.getDayOfWeek() == diaDaSemana
                && !data.isBefore(primeiro)
                && ChronoUnit.WEEKS.between(primeiro, data) % intervaloEmSemanas == 0;
    }

    @Override
    public LocalDate inicioDoPeriodo(LocalDate dataDoPagamento) {
        return dataDoPagamento.minusWeeks(intervaloEmSemanas).plusDays(1);
    }

    @Override
    public BigDecimal parcelaDoSalarioMensal(BigDecimal salarioMensal) {
        return salarioMensal.multiply(MESES_NO_ANO)
                .multiply(BigDecimal.valueOf(intervaloEmSemanas))
                .divide(SEMANAS_NO_ANO, 10, RoundingMode.DOWN);
    }

    private LocalDate primeiroPagamento() {
        return Datas.INICIO_DO_CALENDARIO.with(TemporalAdjusters.nextOrSame(diaDaSemana))
                .plusWeeks(intervaloEmSemanas - 1L);
    }
}
