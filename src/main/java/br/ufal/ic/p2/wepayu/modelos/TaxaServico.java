package br.ufal.ic.p2.wepayu.modelos;

import br.ufal.ic.p2.wepayu.util.Exigir;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Taxa de um servico do sindicato, cobrada no proximo pagamento. Imutavel. */
public record TaxaServico(LocalDate data, BigDecimal valor) implements Serializable {

    private static final long serialVersionUID = 1L;

    public TaxaServico {
        Exigir.naoNulo(data, "Data da taxa de servico");
        Exigir.positivo(valor, "Valor da taxa de servico");
    }
}
