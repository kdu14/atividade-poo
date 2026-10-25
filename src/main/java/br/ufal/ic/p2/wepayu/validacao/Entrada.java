package br.ufal.ic.p2.wepayu.validacao;

import br.ufal.ic.p2.wepayu.excecoes.CampoObrigatorioException;
import br.ufal.ic.p2.wepayu.excecoes.DataInvalidaException;
import br.ufal.ic.p2.wepayu.excecoes.IntervaloDeDatasInvalidoException;
import br.ufal.ic.p2.wepayu.excecoes.ValorInvalidoException;
import br.ufal.ic.p2.wepayu.excecoes.WePayUException;
import br.ufal.ic.p2.wepayu.util.Datas;
import br.ufal.ic.p2.wepayu.util.Dinheiro;
import br.ufal.ic.p2.wepayu.util.Periodo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Converte e valida o texto que chega dos comandos de script. Cada metodo
 * devolve o valor ja no tipo certo ou lanca o erro de negocio com a
 * mensagem que os testes esperam.
 */
public final class Entrada {

    private Entrada() {
    }

    public static String texto(String valor, String mensagemSeVazio) throws CampoObrigatorioException {
        if (valor == null || valor.isEmpty()) {
            throw new CampoObrigatorioException(mensagemSeVazio);
        }
        return valor;
    }

    public static BigDecimal salario(String texto) throws WePayUException {
        return naoNegativo(texto, "Salario nao pode ser nulo.", "Salario deve ser numerico.", "Salario deve ser nao-negativo.");
    }

    public static BigDecimal comissao(String texto) throws WePayUException {
        return naoNegativo(texto, "Comissao nao pode ser nula.", "Comissao deve ser numerica.", "Comissao deve ser nao-negativa.");
    }

    public static BigDecimal taxaSindical(String texto) throws WePayUException {
        return naoNegativo(texto, "Taxa sindical nao pode ser nula.", "Taxa sindical deve ser numerica.",
                "Taxa sindical deve ser nao-negativa.");
    }

    public static BigDecimal positivo(String texto, String mensagemErro) throws ValorInvalidoException {
        if (texto == null || !Dinheiro.ehNumerico(texto)) {
            throw new ValorInvalidoException(mensagemErro);
        }
        BigDecimal valor = Dinheiro.parse(texto);
        if (valor.signum() <= 0) {
            throw new ValorInvalidoException(mensagemErro);
        }
        return valor;
    }

    public static boolean booleano(String texto) throws ValorInvalidoException {
        if ("true".equals(texto)) {
            return true;
        }
        if ("false".equals(texto)) {
            return false;
        }
        throw new ValorInvalidoException("Valor deve ser true ou false.");
    }

    /** Posicao (1, 2, ...) usada por getEmpregadoPorNome. */
    public static int indice(String texto) throws ValorInvalidoException {
        try {
            int indice = Integer.parseInt(texto.trim());
            if (indice > 0) {
                return indice;
            }
        } catch (NumberFormatException | NullPointerException e) {
            // cai no erro de negocio abaixo
        }
        throw new ValorInvalidoException("Indice deve ser um inteiro positivo.");
    }

    public static LocalDate data(String texto, String mensagemErro) throws DataInvalidaException {
        try {
            return Datas.parse(texto);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new DataInvalidaException(mensagemErro);
        }
    }

    public static Periodo periodo(String inicioTexto, String fimTexto) throws WePayUException {
        LocalDate inicio = data(inicioTexto, "Data inicial invalida.");
        LocalDate fim = data(fimTexto, "Data final invalida.");
        if (inicio.isAfter(fim)) {
            throw new IntervaloDeDatasInvalidoException();
        }
        return new Periodo(inicio, fim);
    }

    private static BigDecimal naoNegativo(String texto, String mensagemNulo, String mensagemNaoNumerico,
                                          String mensagemNegativo) throws WePayUException {
        if (texto == null || texto.isEmpty()) {
            throw new CampoObrigatorioException(mensagemNulo);
        }
        if (!Dinheiro.ehNumerico(texto)) {
            throw new ValorInvalidoException(mensagemNaoNumerico);
        }
        BigDecimal valor = Dinheiro.parse(texto);
        if (valor.signum() < 0) {
            throw new ValorInvalidoException(mensagemNegativo);
        }
        return valor;
    }
}
