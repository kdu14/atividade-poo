package br.ufal.ic.p2.wepayu.util;

import java.time.LocalDate;

/**
 * Intervalo de datas [inicio, fim): inclui o primeiro dia e exclui o ultimo,
 * como nas consultas por periodo dos testes (getHorasTrabalhadas etc.).
 */
public record Periodo(LocalDate inicio, LocalDate fim) {

    public Periodo {
        Exigir.naoNulo(inicio, "Inicio do periodo");
        Exigir.naoNulo(fim, "Fim do periodo");
        if (inicio.isAfter(fim)) {
            throw new IllegalArgumentException("Inicio do periodo depois do fim.");
        }
    }

    public boolean contem(LocalDate data) {
        return !data.isBefore(inicio) && data.isBefore(fim);
    }
}
