package br.ufal.ic.p2.wepayu.modelos;

import br.ufal.ic.p2.wepayu.util.Exigir;
import br.ufal.ic.p2.wepayu.util.Periodo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * A filiacao de um empregado ao sindicato: identificacao (que nao e a mesma
 * da folha), taxa sindical diaria, as taxas de servico lancadas e a divida
 * que sobrou de pagamentos que nao cobriram os descontos (Obs. 1 da US7).
 */
public class MembroSindicato implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final BigDecimal taxaSindical;
    private final List<TaxaServico> taxasServico = new ArrayList<>();
    private BigDecimal divida = BigDecimal.ZERO;

    public MembroSindicato(String id, BigDecimal taxaSindical) {
        this.id = Exigir.preenchido(id, "Identificacao do sindicato");
        this.taxaSindical = Exigir.naoNegativo(taxaSindical, "Taxa sindical");
    }

    /** Mesma filiacao com novos dados: as taxas de servico e a divida continuam valendo. */
    public MembroSindicato comNovosDados(String novoId, BigDecimal novaTaxaSindical) {
        MembroSindicato atualizado = new MembroSindicato(novoId, novaTaxaSindical);
        atualizado.taxasServico.addAll(taxasServico);
        atualizado.divida = divida;
        return atualizado;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getTaxaSindical() {
        return taxaSindical;
    }

    public BigDecimal getDivida() {
        return divida;
    }

    /** O que ficou sem descontar no ultimo pagamento; sera cobrado no proximo. */
    public void registrarDivida(BigDecimal divida) {
        this.divida = Exigir.naoNegativo(divida, "Divida sindical");
    }

    public void lancarTaxaServico(TaxaServico taxa) {
        taxasServico.add(Exigir.naoNulo(taxa, "Taxa de servico"));
    }

    public BigDecimal getTaxasServicoNoPeriodo(Periodo periodo) {
        BigDecimal total = BigDecimal.ZERO;
        for (TaxaServico taxa : taxasServico) {
            if (periodo.contem(taxa.data())) {
                total = total.add(taxa.valor());
            }
        }
        return total;
    }
}
