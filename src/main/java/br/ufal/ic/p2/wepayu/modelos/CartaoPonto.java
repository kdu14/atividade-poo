package br.ufal.ic.p2.wepayu.modelos;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CartaoPonto implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalDate data;
    private final BigDecimal horas;

    public CartaoPonto(LocalDate data, BigDecimal horas) {
        this.data = data;
        this.horas = horas;
    }

    public LocalDate getData() {
        return data;
    }

    public BigDecimal getHoras() {
        return horas;
    }
}
