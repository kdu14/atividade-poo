package br.ufal.ic.p2.wepayu.modelos;

import br.ufal.ic.p2.wepayu.util.Exigir;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Uma venda feita por um comissionado. Imutavel depois de criada. */
public record ResultadoVenda(LocalDate data, BigDecimal valor) implements Serializable {

    private static final long serialVersionUID = 1L;

    public ResultadoVenda {
        Exigir.naoNulo(data, "Data da venda");
        Exigir.positivo(valor, "Valor da venda");
    }
}
