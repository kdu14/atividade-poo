package br.ufal.ic.p2.wepayu.util;

import java.math.BigDecimal;

/** Horas nao usam 2 casas fixas como dinheiro: 8, nao 8,00; 1,5, nao 1,50. */
public final class Horas {

    private Horas() {
    }

    public static String formatar(BigDecimal horas) {
        if (horas.signum() == 0) {
            return "0";
        }
        return horas.stripTrailingZeros().toPlainString().replace('.', ',');
    }
}
