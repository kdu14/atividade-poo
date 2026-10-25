package br.ufal.ic.p2.wepayu.modelos;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * O resultado de rodar a folha pra UM empregado, num dia especifico.
 * dividaSindicalRestante e o que nao coube nos descontos deste pagamento
 * e fica para o proximo (Obs. 1 da US7).
 */
public record Pagamento(
        String nome,
        TipoEmpregado tipo,
        SalarioBruto bruto,
        BigDecimal descontos,
        BigDecimal salarioLiquido,
        String metodoDescricao,
        BigDecimal dividaSindicalRestante
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public BigDecimal salarioBruto() {
        return bruto.valor();
    }
}
