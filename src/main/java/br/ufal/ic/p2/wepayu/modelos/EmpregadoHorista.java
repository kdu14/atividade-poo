package br.ufal.ic.p2.wepayu.modelos;

import br.ufal.ic.p2.wepayu.excecoes.EmpregadoNaoEhHoristaException;
import br.ufal.ic.p2.wepayu.util.Datas;
import br.ufal.ic.p2.wepayu.util.Dinheiro;
import br.ufal.ic.p2.wepayu.util.Periodo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Recebe por hora trabalhada; o que passa de 8 horas no dia vale 1,5 hora. */
public class EmpregadoHorista extends Empregado {

    private static final long serialVersionUID = 1L;

    private static final BigDecimal JORNADA_DIARIA = BigDecimal.valueOf(8);
    private static final BigDecimal ADICIONAL_HORA_EXTRA = new BigDecimal("1.5");

    private final List<CartaoPonto> cartoes = new ArrayList<>();

    /**
     * A dataBase fica null ate o primeiro cartao: o enunciado diz que o
     * horista e contratado no primeiro dia em que lancar um cartao.
     */
    public EmpregadoHorista(String id, String nome, String endereco, BigDecimal salarioPorHora) {
        super(id, nome, endereco, salarioPorHora, null);
    }

    /** O proprio empregado, se for horista; senao, o erro de negocio. */
    public static EmpregadoHorista exigir(Empregado empregado) throws EmpregadoNaoEhHoristaException {
        if (empregado instanceof EmpregadoHorista horista) {
            return horista;
        }
        throw new EmpregadoNaoEhHoristaException();
    }

    @Override
    public TipoEmpregado getTipo() {
        return TipoEmpregado.HORISTA;
    }

    public void lancarCartao(CartaoPonto cartao) {
        cartoes.add(cartao);
        if (getDataBase() == null) {
            setDataBase(cartao.data().minusDays(1));
        }
    }

    public BigDecimal getHorasNormaisNoPeriodo(Periodo periodo) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartaoPonto cartao : cartoes) {
            if (periodo.contem(cartao.data())) {
                total = total.add(cartao.horas().min(JORNADA_DIARIA));
            }
        }
        return total;
    }

    public BigDecimal getHorasExtrasNoPeriodo(Periodo periodo) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartaoPonto cartao : cartoes) {
            if (periodo.contem(cartao.data())) {
                total = total.add(cartao.horas().subtract(JORNADA_DIARIA).max(BigDecimal.ZERO));
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
            return novoPagamento(ComposicaoDoBruto.deHorista(BigDecimal.ZERO, BigDecimal.ZERO), BigDecimal.ZERO, data);
        }

        Periodo periodo = new Periodo(getDataBase().plusDays(1), data);
        BigDecimal horasNormais = getHorasNormaisNoPeriodo(periodo);
        BigDecimal horasExtras = getHorasExtrasNoPeriodo(periodo);

        BigDecimal bruto = Dinheiro.truncar(
                horasNormais.multiply(getSalario())
                        .add(horasExtras.multiply(getSalario()).multiply(ADICIONAL_HORA_EXTRA)));

        return novoPagamento(ComposicaoDoBruto.deHorista(horasNormais, horasExtras), bruto, data);
    }
}
