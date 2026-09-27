package br.ufal.ic.p2.wepayu.modelos;

import br.ufal.ic.p2.wepayu.util.Datas;
import br.ufal.ic.p2.wepayu.util.Dinheiro;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmpregadoComissionado extends Empregado {

    // primeiro pagamento: sexta da 2a semana apos a contratacao (1/1/2005,
    // que cai num sabado) -- ou seja, 14/1; dai em diante, a cada 14 dias.
    private static final LocalDate PRIMEIRO_PAGAMENTO = LocalDate.of(2005, 1, 14);
    private static final int DIAS_NO_CICLO = 14;

    /** salario*12/26: o ano tem 52 semanas -> 26 quinzenas (ver User Story 7). */
    private static final BigDecimal DOZE = BigDecimal.valueOf(12);
    private static final BigDecimal VINTE_E_SEIS = BigDecimal.valueOf(26);

    private BigDecimal comissao;
    private final List<ResultadoVenda> vendas = new ArrayList<>();

    public EmpregadoComissionado(String id, String nome, String endereco, BigDecimal salarioMensal, BigDecimal comissao) {
        super(id, nome, endereco, salarioMensal);
        this.comissao = comissao;
        setDataBase(EmpregadoAssalariado.DATA_CONTRATACAO_PADRAO.minusDays(1));
    }

    @Override
    public String getTipo() {
        return "comissionado";
    }

    @Override
    public BigDecimal getComissao() {
        return comissao;
    }

    @Override
    public void setComissao(BigDecimal comissao) {
        this.comissao = comissao;
    }

    @Override
    public void lancarVenda(LocalDate data, BigDecimal valor) {
        vendas.add(new ResultadoVenda(data, valor));
    }

    @Override
    public BigDecimal getVendasNoPeriodo(LocalDate inicioInclusivo, LocalDate fimExclusivo) {
        BigDecimal total = BigDecimal.ZERO;
        for (ResultadoVenda v : vendas) {
            if (Datas.dentroDoIntervalo(v.getData(), inicioInclusivo, fimExclusivo)) {
                total = total.add(v.getValor());
            }
        }
        return total;
    }

    @Override
    public boolean estaNaDataDePagamento(LocalDate data) {
        if (data.isBefore(PRIMEIRO_PAGAMENTO) || !Datas.ehSextaFeira(data)) {
            return false;
        }
        long dias = Datas.diasEntre(PRIMEIRO_PAGAMENTO, data);
        return dias % DIAS_NO_CICLO == 0;
    }

    @Override
    protected Pagamento calcularPagamentoNovo(LocalDate data) {
        LocalDate inicioPeriodo = getDataBase().plusDays(1);

        BigDecimal fixo = Dinheiro.dividirETruncar(getSalario().multiply(DOZE), VINTE_E_SEIS);
        BigDecimal vendasNoPeriodo = getVendasNoPeriodo(inicioPeriodo, data);
        BigDecimal comissaoDevida = Dinheiro.truncar(vendasNoPeriodo.multiply(comissao));
        BigDecimal bruto = fixo.add(comissaoDevida);

        ResultadoDesconto resultado = calcularDescontos(data, bruto);

        return new Pagamento(getNome(), getTipo(), BigDecimal.ZERO, BigDecimal.ZERO,
                fixo, vendasNoPeriodo, comissaoDevida,
                bruto, resultado.aplicados(), resultado.liquido(),
                getMetodoPagamento().getDescricao(getEndereco()), resultado.avancaPeriodo());
    }
}
