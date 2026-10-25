package br.ufal.ic.p2.wepayu.modelos;

import br.ufal.ic.p2.wepayu.excecoes.EmpregadoNaoEhComissionadoException;
import br.ufal.ic.p2.wepayu.util.Datas;
import br.ufal.ic.p2.wepayu.util.Dinheiro;
import br.ufal.ic.p2.wepayu.util.Exigir;
import br.ufal.ic.p2.wepayu.util.Periodo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Assalariado que tambem recebe um percentual das vendas que realiza. */
public class EmpregadoComissionado extends Empregado {

    private static final long serialVersionUID = 1L;

    // primeiro pagamento: sexta da 2a semana apos a contratacao (1/1/2005,
    // que cai num sabado) -- ou seja, 14/1; dai em diante, a cada 14 dias.
    private static final LocalDate PRIMEIRO_PAGAMENTO = LocalDate.of(2005, 1, 14);
    private static final int DIAS_NO_CICLO = 14;

    /** salario*12/26: o ano tem 52 semanas -> 26 quinzenas (ver User Story 7). */
    private static final BigDecimal MESES_NO_ANO = BigDecimal.valueOf(12);
    private static final BigDecimal QUINZENAS_NO_ANO = BigDecimal.valueOf(26);

    private BigDecimal comissao;
    private final List<ResultadoVenda> vendas = new ArrayList<>();

    public EmpregadoComissionado(String id, String nome, String endereco, BigDecimal salarioMensal, BigDecimal comissao) {
        super(id, nome, endereco, salarioMensal, DATA_CONTRATACAO_PADRAO.minusDays(1));
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
    public boolean estaNaDataDePagamento(LocalDate data) {
        if (data.isBefore(PRIMEIRO_PAGAMENTO) || !Datas.ehSextaFeira(data)) {
            return false;
        }
        return Datas.diasEntre(PRIMEIRO_PAGAMENTO, data) % DIAS_NO_CICLO == 0;
    }

    @Override
    protected Pagamento calcularPagamentoNovo(LocalDate data) {
        BigDecimal fixo = Dinheiro.dividirETruncar(getSalario().multiply(MESES_NO_ANO), QUINZENAS_NO_ANO);
        BigDecimal vendasNoPeriodo = getVendasNoPeriodo(new Periodo(getDataBase().plusDays(1), data));
        BigDecimal comissaoDevida = Dinheiro.truncar(vendasNoPeriodo.multiply(comissao));

        return novoPagamento(ComposicaoDoBruto.deComissionado(fixo, vendasNoPeriodo, comissaoDevida),
                fixo.add(comissaoDevida), data);
    }
}
