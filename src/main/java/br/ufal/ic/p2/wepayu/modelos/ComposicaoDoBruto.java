package br.ufal.ic.p2.wepayu.modelos;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * De onde veio o salario bruto de um pagamento. Horas so existem para
 * horista; fixo, vendas e comissao so para comissionado -- os construtores
 * nomeados deixam isso explicito em vez de passar zeros soltos.
 */
public record ComposicaoDoBruto(BigDecimal horasNormais, BigDecimal horasExtras,
                                BigDecimal salarioFixo, BigDecimal vendas, BigDecimal comissao)
        implements Serializable {

    private static final long serialVersionUID = 1L;

    public static ComposicaoDoBruto deHorista(BigDecimal horasNormais, BigDecimal horasExtras) {
        return new ComposicaoDoBruto(horasNormais, horasExtras, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public static ComposicaoDoBruto deComissionado(BigDecimal salarioFixo, BigDecimal vendas, BigDecimal comissao) {
        return new ComposicaoDoBruto(BigDecimal.ZERO, BigDecimal.ZERO, salarioFixo, vendas, comissao);
    }

    /** Assalariado: o bruto e o proprio salario, nao ha o que detalhar. */
    public static ComposicaoDoBruto semDetalhe() {
        return new ComposicaoDoBruto(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
