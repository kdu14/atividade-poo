package br.ufal.ic.p2.wepayu.modelos;

import br.ufal.ic.p2.wepayu.util.Datas;
import br.ufal.ic.p2.wepayu.util.Dinheiro;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmpregadoHorista extends Empregado {

    private static final BigDecimal OITO_HORAS = BigDecimal.valueOf(8);
    private static final BigDecimal FATOR_HORA_EXTRA = new BigDecimal("1.5");

    private final List<CartaoPonto> cartoes = new ArrayList<>();

    public EmpregadoHorista(String id, String nome, String endereco, BigDecimal salarioPorHora) {
        super(id, nome, endereco, salarioPorHora);
        // dataBase fica null ate o primeiro cartao: o enunciado diz que o
        // horista "e contratado no primeiro dia em que lancar um cartao".
    }

    @Override
    public String getTipo() {
        return "horista";
    }

    @Override
    public void lancarCartao(LocalDate data, BigDecimal horas) {
        cartoes.add(new CartaoPonto(data, horas));
        if (getDataBase() == null) {
            setDataBase(data.minusDays(1));
        }
    }

    @Override
    public BigDecimal getHorasNormaisNoPeriodo(LocalDate inicioInclusivo, LocalDate fimExclusivo) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartaoPonto c : cartoes) {
            if (Datas.dentroDoIntervalo(c.getData(), inicioInclusivo, fimExclusivo)) {
                total = total.add(c.getHoras().min(OITO_HORAS));
            }
        }
        return total;
    }

    @Override
    public BigDecimal getHorasExtrasNoPeriodo(LocalDate inicioInclusivo, LocalDate fimExclusivo) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartaoPonto c : cartoes) {
            if (Datas.dentroDoIntervalo(c.getData(), inicioInclusivo, fimExclusivo)) {
                BigDecimal extra = c.getHoras().subtract(OITO_HORAS);
                if (extra.compareTo(BigDecimal.ZERO) > 0) {
                    total = total.add(extra);
                }
            }
        }
        return total;
    }

    @Override
    public boolean estaNaDataDePagamento(LocalDate data) {
        // todo horista e pago toda sexta-feira, tenha ele batido ponto ou nao.
        return Datas.ehSextaFeira(data);
    }

    @Override
    protected Pagamento calcularPagamentoNovo(LocalDate data) {
        if (getDataBase() == null) {
            // nunca lancou cartao: aparece na folha, mas nao ha nada a calcular ainda.
            return new Pagamento(getNome(), getTipo(), BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    getMetodoPagamento().getDescricao(getEndereco()), false);
        }

        LocalDate inicioPeriodo = getDataBase().plusDays(1);
        BigDecimal horasNormais = getHorasNormaisNoPeriodo(inicioPeriodo, data);
        BigDecimal horasExtras = getHorasExtrasNoPeriodo(inicioPeriodo, data);

        BigDecimal bruto = Dinheiro.truncar(
                horasNormais.multiply(getSalario())
                        .add(horasExtras.multiply(getSalario()).multiply(FATOR_HORA_EXTRA)));

        ResultadoDesconto resultado = calcularDescontos(data, bruto);

        return new Pagamento(getNome(), getTipo(), horasNormais, horasExtras,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                bruto, resultado.aplicados(), resultado.liquido(),
                getMetodoPagamento().getDescricao(getEndereco()), resultado.avancaPeriodo());
    }
}
