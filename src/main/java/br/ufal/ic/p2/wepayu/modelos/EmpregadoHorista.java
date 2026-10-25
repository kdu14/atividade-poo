package br.ufal.ic.p2.wepayu.modelos;

import br.ufal.ic.p2.wepayu.excecoes.EmpregadoNaoEhHoristaException;
import br.ufal.ic.p2.wepayu.modelos.agenda.AgendaPagamento;
import br.ufal.ic.p2.wepayu.util.Dinheiro;
import br.ufal.ic.p2.wepayu.util.Periodo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Recebe por hora trabalhada; o que passa de 8 horas no dia vale 1,5 hora. */
public class EmpregadoHorista extends Empregado {

    private static final long serialVersionUID = 1L;

    private static final BigDecimal JORNADA_DIARIA = BigDecimal.valueOf(8);
    private static final BigDecimal ADICIONAL_HORA_EXTRA = new BigDecimal("1.5");

    private final List<CartaoPonto> cartoes = new ArrayList<>();

    /** Sem data de contratacao: ela e o dia do primeiro cartao lancado. */
    public EmpregadoHorista(String id, String nome, String endereco, BigDecimal salarioPorHora, AgendaPagamento agenda) {
        super(id, nome, endereco, salarioPorHora, agenda, null);
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
        if (!jaContratado()) {
            definirContratacao(cartao.data());
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
    protected SalarioBruto calcularBruto(Periodo periodo) {
        BigDecimal horasNormais = getHorasNormaisNoPeriodo(periodo);
        BigDecimal horasExtras = getHorasExtrasNoPeriodo(periodo);
        BigDecimal valor = Dinheiro.truncar(
                horasNormais.multiply(getSalario())
                        .add(horasExtras.multiply(getSalario()).multiply(ADICIONAL_HORA_EXTRA)));
        return SalarioBruto.deHorista(valor, horasNormais, horasExtras);
    }
}
