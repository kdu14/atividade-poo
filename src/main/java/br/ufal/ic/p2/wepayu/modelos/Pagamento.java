package br.ufal.ic.p2.wepayu.modelos;

import java.io.Serializable;
import java.math.BigDecimal;

/** O resultado de rodar a folha pra UM empregado, num dia especifico. */
public record Pagamento(
        String nome,
        TipoEmpregado tipo,
        ComposicaoDoBruto composicao,
        BigDecimal salarioBruto,
        BigDecimal descontos,
        BigDecimal salarioLiquido,
        String metodoDescricao,
        boolean avancaPeriodo
) implements Serializable {

    private static final long serialVersionUID = 1L;
}
