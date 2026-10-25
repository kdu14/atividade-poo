package br.ufal.ic.p2.wepayu.modelos;

import br.ufal.ic.p2.wepayu.excecoes.EmpregadoNaoEhComissionadoException;
import br.ufal.ic.p2.wepayu.modelos.agenda.AgendaPagamento;
import br.ufal.ic.p2.wepayu.util.Datas;
import br.ufal.ic.p2.wepayu.util.Dinheiro;
import br.ufal.ic.p2.wepayu.util.Exigir;
import br.ufal.ic.p2.wepayu.util.Periodo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Assalariado que tambem recebe um percentual das vendas que realiza. */
public class EmpregadoComissionado extends Empregado {

    private static final long serialVersionUID = 1L;

    private BigDecimal comissao;
    private final List<ResultadoVenda> vendas = new ArrayList<>();

    public EmpregadoComissionado(String id, String nome, String endereco, BigDecimal salarioMensal,
                                 BigDecimal comissao, AgendaPagamento agenda) {
        super(id, nome, endereco, salarioMensal, agenda, Datas.INICIO_DO_CALENDARIO);
        this.comissao = Exigir.naoNegativo(comissao, "Comissao");
    }

    /** O proprio empregado, se for comissionado; senao, o erro de negocio. */
    public static EmpregadoComissionado exigir(Empregado empregado) throws EmpregadoNaoEhComissionadoException {
        if (empregado instanceof EmpregadoComissionado comissionado) {
            return comissionado;
        }
        throw new EmpregadoNaoEhComissionadoException();
    }

    @Override
    public TipoEmpregado getTipo() {
        return TipoEmpregado.COMISSIONADO;
    }

    public BigDecimal getComissao() {
        return comissao;
    }

    public void setComissao(BigDecimal comissao) {
        this.comissao = Exigir.naoNegativo(comissao, "Comissao");
    }

    public void lancarVenda(ResultadoVenda venda) {
        vendas.add(venda);
    }

    public BigDecimal getVendasNoPeriodo(Periodo periodo) {
        BigDecimal total = BigDecimal.ZERO;
        for (ResultadoVenda venda : vendas) {
            if (periodo.contem(venda.data())) {
                total = total.add(venda.valor());
            }
        }
        return total;
    }

    @Override
    protected SalarioBruto calcularBruto(Periodo periodo) {
        BigDecimal vendasNoPeriodo = getVendasNoPeriodo(periodo);
        BigDecimal comissaoDevida = Dinheiro.truncar(vendasNoPeriodo.multiply(comissao));
        return SalarioBruto.deComissionado(parcelaDoSalarioFixo(), vendasNoPeriodo, comissaoDevida);
    }
}
