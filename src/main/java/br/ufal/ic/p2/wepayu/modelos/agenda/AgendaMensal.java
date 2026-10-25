package br.ufal.ic.p2.wepayu.modelos.agenda;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Paga uma vez por mes; cada pagamento leva o salario mensal inteiro. As
 * subclasses so dizem qual e o dia do pagamento em cada mes.
 */
public abstract class AgendaMensal extends AgendaPagamento {

    private static final long serialVersionUID = 1L;

    protected AgendaMensal(String descricao) {
        super(descricao);
    }

    /** O dia de pagamento no mes da data informada. */
    protected abstract LocalDate diaDePagamentoNoMes(LocalDate qualquerDiaDoMes);

    @Override
    public boolean ehDiaDePagamento(LocalDate data) {
        return data.equals(diaDePagamentoNoMes(data));
    }

    @Override
    public LocalDate inicioDoPeriodo(LocalDate dataDoPagamento) {
        return diaDePagamentoNoMes(dataDoPagamento.minusMonths(1)).plusDays(1);
    }

    @Override
    public BigDecimal parcelaDoSalarioMensal(BigDecimal salarioMensal) {
        return salarioMensal;
    }
}
