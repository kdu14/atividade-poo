package br.ufal.ic.p2.wepayu.relatorio;

import br.ufal.ic.p2.wepayu.modelos.Pagamento;
import br.ufal.ic.p2.wepayu.modelos.TipoEmpregado;
import br.ufal.ic.p2.wepayu.util.Dinheiro;
import br.ufal.ic.p2.wepayu.util.Horas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Monta o relatorio de rodaFolha exatamente como os gabaritos em ok/*.txt
 * (o teste compara byte a byte). As tres secoes tem a mesma estrutura e so
 * mudam as colunas, entao cada secao e descrita por uma tabela de colunas e
 * desenhada por uma rotina so. Larguras medidas nos arquivos de gabarito.
 */
public final class RelatorioDaFolha {

    private static final int LARGURA_TOTAL = 127;
    private static final int RECUO_DO_TITULO_DA_SECAO = 21;
    private static final DateTimeFormatter FORMATO_DA_DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** Uma coluna numerica: titulo, largura, de onde vem o valor e como ele e escrito. */
    private record Coluna(String titulo, int largura, Function<Pagamento, BigDecimal> valor,
                          Function<BigDecimal, String> formato) {
    }

    private record Secao(TipoEmpregado tipo, String titulo, int larguraDoNome, List<Coluna> colunas) {
    }

    private static final Coluna BRUTO = new Coluna("Salario Bruto", 13, Pagamento::salarioBruto, Dinheiro::formatar);
    private static final Coluna DESCONTOS = new Coluna("Descontos", 9, Pagamento::descontos, Dinheiro::formatar);
    private static final Coluna LIQUIDO = new Coluna("Salario Liquido", 15, Pagamento::salarioLiquido, Dinheiro::formatar);

    private static final List<Secao> SECOES = List.of(
            new Secao(TipoEmpregado.HORISTA, "HORISTAS", 36, List.of(
                    new Coluna("Horas", 5, p -> p.bruto().horasNormais(), Horas::formatar),
                    new Coluna("Extra", 5, p -> p.bruto().horasExtras(), Horas::formatar),
                    BRUTO, DESCONTOS, LIQUIDO)),
            new Secao(TipoEmpregado.ASSALARIADO, "ASSALARIADOS", 48, List.of(BRUTO, DESCONTOS, LIQUIDO)),
            new Secao(TipoEmpregado.COMISSIONADO, "COMISSIONADOS", 21, List.of(
                    new Coluna("Fixo", 8, p -> p.bruto().salarioFixo(), Dinheiro::formatar),
                    new Coluna("Vendas", 8, p -> p.bruto().vendas(), Dinheiro::formatar),
                    new Coluna("Comissao", 8, p -> p.bruto().comissao(), Dinheiro::formatar),
                    BRUTO, DESCONTOS, LIQUIDO)));

    private RelatorioDaFolha() {
    }

    public static String montar(LocalDate data, Collection<Pagamento> pagamentos) {
        StringBuilder relatorio = new StringBuilder();
        String titulo = "FOLHA DE PAGAMENTO DO DIA " + data.format(FORMATO_DA_DATA);
        relatorio.append(titulo).append('\n')
                .append("=".repeat(titulo.length())).append('\n')
                .append('\n');

        for (Secao secao : SECOES) {
            relatorio.append(desenhar(secao, doTipoEmOrdemAlfabetica(pagamentos, secao.tipo())));
        }

        relatorio.append("TOTAL FOLHA: ").append(Dinheiro.formatar(somar(pagamentos, Pagamento::salarioBruto))).append('\n');
        return relatorio.toString();
    }

    private static String desenhar(Secao secao, List<Pagamento> linhas) {
        StringBuilder texto = new StringBuilder();
        texto.append(divisor()).append(tituloDaSecao(secao.titulo())).append(divisor());

        StringBuilder cabecalho = new StringBuilder(alinharAEsquerda("Nome", secao.larguraDoNome()));
        StringBuilder sublinhado = new StringBuilder("=".repeat(secao.larguraDoNome()));
        for (Coluna coluna : secao.colunas()) {
            cabecalho.append(' ').append(alinharAEsquerda(coluna.titulo(), coluna.largura()));
            sublinhado.append(' ').append("=".repeat(coluna.largura()));
        }
        texto.append(cabecalho).append(" Metodo\n");
        texto.append(sublinhado).append(' ').append("=".repeat(LARGURA_TOTAL - sublinhado.length() - 1)).append('\n');

        for (Pagamento pagamento : linhas) {
            texto.append(alinharAEsquerda(pagamento.nome(), secao.larguraDoNome()));
            for (Coluna coluna : secao.colunas()) {
                texto.append(' ').append(alinharADireita(coluna.formato().apply(coluna.valor().apply(pagamento)), coluna.largura()));
            }
            texto.append(' ').append(pagamento.metodoDescricao()).append('\n');
        }

        texto.append('\n').append(alinharAEsquerda("TOTAL " + secao.titulo(), secao.larguraDoNome()));
        for (Coluna coluna : secao.colunas()) {
            texto.append(' ').append(alinharADireita(coluna.formato().apply(somar(linhas, coluna.valor())), coluna.largura()));
        }
        return texto.append("\n\n").toString();
    }

    private static List<Pagamento> doTipoEmOrdemAlfabetica(Collection<Pagamento> pagamentos, TipoEmpregado tipo) {
        // em cada secao, ordem alfabetica de nome, e nao de criacao (conferido contra ok/*.txt)
        return pagamentos.stream()
                .filter(p -> p.tipo() == tipo)
                .sorted(Comparator.comparing(Pagamento::nome))
                .toList();
    }

    private static BigDecimal somar(Collection<Pagamento> pagamentos, Function<Pagamento, BigDecimal> valor) {
        BigDecimal total = BigDecimal.ZERO;
        for (Pagamento pagamento : pagamentos) {
            total = total.add(valor.apply(pagamento));
        }
        return total;
    }

    private static String divisor() {
        return "=".repeat(LARGURA_TOTAL) + "\n";
    }

    private static String tituloDaSecao(String nome) {
        String inicio = "=".repeat(RECUO_DO_TITULO_DA_SECAO) + " " + nome + " ";
        return inicio + "=".repeat(LARGURA_TOTAL - inicio.length()) + "\n";
    }

    private static String alinharAEsquerda(String texto, int largura) {
        return String.format("%-" + largura + "s", texto);
    }

    private static String alinharADireita(String texto, int largura) {
        return String.format("%" + largura + "s", texto);
    }
}
