package br.ufal.ic.p2.wepayu.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Dinheiro {

    private Dinheiro() {
    }

    // TRUNCAMENTO, nao arredondamento comum: confirmado comparando o
    // relatorio oficial (ok/folha-2005-01-14.txt) com a formula do fixo do
    // comissionado. Salario 1500 -> 1500*12/26 = 692,3076923..., e o
    // relatorio mostra 692,30 (arredondamento normal daria 692,31).
    public static BigDecimal truncar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.DOWN);
    }

    // escala interna maior (10 casas) antes de truncar, pra divisoes como
    // 1500*12/26 nao perderem precisao antes da hora.
    public static BigDecimal dividirETruncar(BigDecimal numerador, BigDecimal denominador) {
        return truncar(numerador.divide(denominador, 10, RoundingMode.DOWN));
    }

    // 2 casas, virgula decimal, SEM separador de milhar (2803,04, nao
    // 2.803,04 -- por isso nao da pra usar Locale pt_BR direto no format).
    public static String formatar(BigDecimal valor) {
        BigDecimal truncado = truncar(valor);
        return truncado.toPlainString().replace('.', ',');
    }

    public static BigDecimal parse(String valor) {
        String normalizado = valor.trim().replace(",", ".");
        return new BigDecimal(normalizado);
    }

    public static boolean ehNumerico(String valor) {
        try {
            parse(valor);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
