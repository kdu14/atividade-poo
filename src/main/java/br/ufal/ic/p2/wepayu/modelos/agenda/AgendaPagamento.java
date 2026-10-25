package br.ufal.ic.p2.wepayu.modelos.agenda;

import br.ufal.ic.p2.wepayu.util.Exigir;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Quando um empregado e pago e quanto do salario mensal cabe em cada
 * pagamento. Duas agendas sao iguais quando tem a mesma descricao (o texto
 * usado nos comandos, ex.: "semanal 2 5").
 */
public abstract class AgendaPagamento implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String descricao;

    protected AgendaPagamento(String descricao) {
        this.descricao = Exigir.preenchido(descricao, "Descricao da agenda");
    }

    public String getDescricao() {
        return descricao;
    }

    public abstract boolean ehDiaDePagamento(LocalDate data);

    /** Primeiro dia coberto pelo pagamento desta data: o dia seguinte ao pagamento anterior. */
    public abstract LocalDate inicioDoPeriodo(LocalDate dataDoPagamento);

    /** A parte do salario mensal que cabe a um pagamento nesta agenda (sem truncar). */
    public abstract BigDecimal parcelaDoSalarioMensal(BigDecimal salarioMensal);

    @Override
    public boolean equals(Object outro) {
        return outro instanceof AgendaPagamento agenda && descricao.equals(agenda.descricao);
    }

    @Override
    public int hashCode() {
        return descricao.hashCode();
    }

    @Override
    public String toString() {
        return descricao;
    }
}
