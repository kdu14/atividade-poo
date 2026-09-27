package br.ufal.ic.p2.wepayu.modelos;

import br.ufal.ic.p2.wepayu.util.Datas;
import br.ufal.ic.p2.wepayu.util.Dinheiro;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EmpregadoAssalariado extends Empregado {

    // O enunciado (US7) pede pra considerar todo assalariado/comissionado
    // contratado em 1/1/2005, ja que criarEmpregado ainda nao tem esse campo.
    static final LocalDate DATA_CONTRATACAO_PADRAO = LocalDate.of(2005, 1, 1);

    public EmpregadoAssalariado(String id, String nome, String endereco, BigDecimal salarioMensal) {
        super(id, nome, endereco, salarioMensal);
        setDataBase(DATA_CONTRATACAO_PADRAO.minusDays(1));
    }

    @Override
    public String getTipo() {
        return "assalariado";
    }

    @Override
    public boolean estaNaDataDePagamento(LocalDate data) {
        return data.equals(Datas.ultimoDiaUtilDoMes(data));
    }

    @Override
    protected Pagamento calcularPagamentoNovo(LocalDate data) {
        BigDecimal bruto = Dinheiro.truncar(getSalario());
        ResultadoDesconto resultado = calcularDescontos(data, bruto);

        return new Pagamento(getNome(), getTipo(), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                bruto, resultado.aplicados(), resultado.liquido(),
                getMetodoPagamento().getDescricao(getEndereco()), resultado.avancaPeriodo());
    }
}
