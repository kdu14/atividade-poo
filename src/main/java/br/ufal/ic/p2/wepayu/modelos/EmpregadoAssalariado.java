package br.ufal.ic.p2.wepayu.modelos;

import br.ufal.ic.p2.wepayu.util.Datas;
import br.ufal.ic.p2.wepayu.util.Dinheiro;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Recebe um salario fixo mensal. */
public class EmpregadoAssalariado extends Empregado {

    private static final long serialVersionUID = 1L;

    public EmpregadoAssalariado(String id, String nome, String endereco, BigDecimal salarioMensal) {
        super(id, nome, endereco, salarioMensal, DATA_CONTRATACAO_PADRAO.minusDays(1));
    }

    @Override
    public TipoEmpregado getTipo() {
        return TipoEmpregado.ASSALARIADO;
    }

    @Override
    public boolean estaNaDataDePagamento(LocalDate data) {
        return data.equals(Datas.ultimoDiaUtilDoMes(data));
    }

    @Override
    protected Pagamento calcularPagamentoNovo(LocalDate data) {
        return novoPagamento(ComposicaoDoBruto.semDetalhe(), Dinheiro.truncar(getSalario()), data);
    }
}
