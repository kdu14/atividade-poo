package br.ufal.ic.p2.wepayu.util;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;

// datas no formato d/M/yyyy do enunciado (ex.: "1/1/2005"); nao decide
// mensagem de erro, isso varia por comando e fica por conta de quem chama.
public final class Datas {

    // uuuu (ano proleptico), nao yyyy (ano-da-era): com STRICT, yyyy exige
    // uma era explicita e sempre falha sem ela. E STRICT que faz 30/2/2005
    // dar erro de verdade -- o resolvedor padrao ("smart") so ajustaria pra
    // 28/2 sem avisar nada.
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("d/M/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    /**
     * Onde o calendario do sistema comeca: o enunciado (US7) manda assumir
     * que assalariados e comissionados foram contratados em 1/1/2005, e as
     * agendas semanais contam as semanas a partir dai.
     */
    public static final LocalDate INICIO_DO_CALENDARIO = LocalDate.of(2005, 1, 1);

    private Datas() {
    }

    public static LocalDate parse(String texto) {
        try {
            return LocalDate.parse(texto.trim(), FORMATO);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Data invalida: " + texto, e);
        }
    }

    public static long diasEntre(LocalDate inicioExclusivo, LocalDate fim) {
        return ChronoUnit.DAYS.between(inicioExclusivo, fim);
    }

    /**
     * Ultimo dia UTIL (segunda a sexta) do mes daquela data, desconsiderando
     * feriados -- o enunciado pede exatamente isso ("desconsidere feriados").
     */
    public static LocalDate ultimoDiaUtilDoMes(LocalDate dataNoMes) {
        LocalDate ultimoDia = YearMonth.from(dataNoMes).atEndOfMonth();
        while (ultimoDia.getDayOfWeek() == DayOfWeek.SATURDAY
                || ultimoDia.getDayOfWeek() == DayOfWeek.SUNDAY) {
            ultimoDia = ultimoDia.minusDays(1);
        }
        return ultimoDia;
    }
}
