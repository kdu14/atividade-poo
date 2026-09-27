package br.ufal.ic.p2.wepayu.modelos;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * O resultado de rodar a folha pra UM empregado, num dia especifico. Nem
 * todo campo vale pra todo tipo (horasNormais/Extras so pra horista,
 * fixo/vendas/comissao so pra comissionado) -- os que nao se aplicam ficam
 * em BigDecimal.ZERO.
 */
public record Pagamento(
        String nome,
        String tipo,
        BigDecimal horasNormais,
        BigDecimal horasExtras,
        BigDecimal salarioFixo,
        BigDecimal vendas,
        BigDecimal comissao,
        BigDecimal salarioBruto,
        BigDecimal descontos,
        BigDecimal salarioLiquido,
        String metodoDescricao,
        boolean avancaPeriodo
) implements Serializable {
}
