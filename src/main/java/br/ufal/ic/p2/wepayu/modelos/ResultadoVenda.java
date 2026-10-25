package br.ufal.ic.p2.wepayu.modelos;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ResultadoVenda implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalDate data;
    private final BigDecimal valor;

    public ResultadoVenda(LocalDate data, BigDecimal valor) {
        this.data = data;
        this.valor = valor;
    }

    public LocalDate getData() {
        return data;
    }

    public BigDecimal getValor() {
        return valor;
    }
}
