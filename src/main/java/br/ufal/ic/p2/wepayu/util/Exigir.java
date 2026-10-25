package br.ufal.ic.p2.wepayu.util;

import java.math.BigDecimal;

/**
 * Invariantes dos objetos de modelo. Os servicos validam a entrada do usuario
 * antes, com a mensagem de negocio que os testes esperam; se algo invalido
 * chegar ate aqui e erro de programacao, e o objeto se recusa a ficar num
 * estado invalido em vez de aceitar em silencio.
 */
public final class Exigir {

    private Exigir() {
    }

    public static <T> T naoNulo(T valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException(campo + " nao pode ser nulo.");
        }
        return valor;
    }

    public static String preenchido(String valor, String campo) {
        if (valor == null || valor.isEmpty()) {
            throw new IllegalArgumentException(campo + " nao pode ser vazio.");
        }
        return valor;
    }

    public static BigDecimal naoNegativo(BigDecimal valor, String campo) {
        if (naoNulo(valor, campo).signum() < 0) {
            throw new IllegalArgumentException(campo + " nao pode ser negativo.");
        }
        return valor;
    }

    public static BigDecimal positivo(BigDecimal valor, String campo) {
        if (naoNulo(valor, campo).signum() <= 0) {
            throw new IllegalArgumentException(campo + " deve ser positivo.");
        }
        return valor;
    }
}
