package br.ufal.ic.p2.wepayu.modelos;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * O salario bruto de um pagamento e de onde ele veio. Horas so existem para
 * horista; fixo, vendas e comissao so para comissionado -- os construtores
 * nomeados deixam isso explicito em vez de passar zeros soltos.
 */
public record SalarioBruto(BigDecimal valor, BigDecimal horasNormais, BigDecimal horasExtras,
                           BigDecimal salarioFixo, BigDecimal vendas, BigDecimal comissao)
        implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    public static SalarioBruto deHorista(BigDecimal valor, BigDecimal horasNormais, BigDecimal horasExtras) {
        return new SalarioBruto(valor, horasNormais, horasExtras, ZERO, ZERO, ZERO);
    }

    public static SalarioBruto deComissionado(BigDecimal salarioFixo, BigDecimal vendas, BigDecimal comissao) {
        return new SalarioBruto(salarioFixo.add(comissao), ZERO, ZERO, salarioFixo, vendas, comissao);
    }

    /** Assalariado: o bruto e o proprio salario, nao ha o que detalhar. */
    public static SalarioBruto fixo(BigDecimal valor) {
        return new SalarioBruto(valor, ZERO, ZERO, ZERO, ZERO, ZERO);
    }

    /** Nada a receber: ainda nao contratado na data (ex.: horista sem cartao). */
    public static SalarioBruto nenhum() {
        return fixo(ZERO);
    }
}
