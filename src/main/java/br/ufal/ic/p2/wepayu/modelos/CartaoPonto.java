package br.ufal.ic.p2.wepayu.modelos;

import br.ufal.ic.p2.wepayu.util.Exigir;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Horas trabalhadas por um horista num dia. Imutavel depois de criado. */
public record CartaoPonto(LocalDate data, BigDecimal horas) implements Serializable {

    private static final long serialVersionUID = 1L;

    public CartaoPonto {
        Exigir.naoNulo(data, "Data do cartao");
        Exigir.positivo(horas, "Horas do cartao");
    }
}
