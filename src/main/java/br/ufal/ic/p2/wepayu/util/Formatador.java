package br.ufal.ic.p2.wepayu.util;

import br.ufal.ic.p2.wepayu.modelos.Pagamento;
import br.ufal.ic.p2.wepayu.modelos.TipoEmpregado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Monta o relatorio de folha (rodaFolha) exatamente como o arquivo de
 * gabarito em ok/*.txt -- o teste de aceitacao compara byte a byte
 * (equalFiles), entao as larguras de coluna abaixo foram medidas direto
 * nos arquivos de gabarito, nao inventadas.
 */
public class Formatador {

    private static final int LARGURA_TOTAL = 127;
    private static final DateTimeFormatter FORMATO_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Formatador() {
    }

    public static String montarRelatorio(LocalDate data, Collection<Pagamento> pagamentos) {
        // o relatorio lista cada secao em ordem alfabetica de nome, nao na
        // ordem em que os empregados foram criados (conferido contra ok/*.txt).
        List<Pagamento> horistas = doTipoPorNome(pagamentos, TipoEmpregado.HORISTA);
        List<Pagamento> assalariados = doTipoPorNome(pagamentos, TipoEmpregado.ASSALARIADO);
        List<Pagamento> comissionados = doTipoPorNome(pagamentos, TipoEmpregado.COMISSIONADO);

        StringBuilder sb = new StringBuilder();

        String titulo = "FOLHA DE PAGAMENTO DO DIA " + data.format(FORMATO_ISO);
        sb.append(titulo).append('\n');
        sb.append("=".repeat(titulo.length())).append('\n');
        sb.append('\n');

        sb.append(secaoHoristas(horistas));
        sb.append(secaoAssalariados(assalariados));
        sb.append(secaoComissionados(comissionados));

        BigDecimal totalFolha = somarBruto(horistas).add(somarBruto(assalariados)).add(somarBruto(comissionados));
        sb.append("TOTAL FOLHA: ").append(Dinheiro.formatar(totalFolha)).append('\n');

        return sb.toString();
    }

    private static List<Pagamento> doTipoPorNome(Collection<Pagamento> pagamentos, TipoEmpregado tipo) {
        List<Pagamento> doTipo = new ArrayList<>();
        for (Pagamento p : pagamentos) {
            if (p.tipo() == tipo) {
                doTipo.add(p);
            }
        }
        doTipo.sort(Comparator.comparing(Pagamento::nome));
        return doTipo;
    }

    private static BigDecimal somarBruto(List<Pagamento> linhas) {
        BigDecimal total = BigDecimal.ZERO;
        for (Pagamento p : linhas) {
            total = total.add(p.salarioBruto());
        }
        return total;
    }

    // ---- HORISTAS: Nome(36) Horas(5) Extra(5) Bruto(13) Descontos(9) Liquido(15) Metodo ----

    private static String secaoHoristas(List<Pagamento> linhas) {
        StringBuilder sb = new StringBuilder();
        sb.append(divisor()).append(tituloSecao("HORISTAS")).append(divisor());
        sb.append(padDireita("Nome", 36)).append(' ')
                .append(padDireita("Horas", 5)).append(' ')
                .append(padDireita("Extra", 5)).append(' ')
                .append(padDireita("Salario Bruto", 13)).append(' ')
                .append(padDireita("Descontos", 9)).append(' ')
                .append(padDireita("Salario Liquido", 15)).append(' ')
                .append("Metodo").append('\n');
        sb.append(sublinhado(36, 5, 5, 13, 9, 15)).append('\n');

        BigDecimal totalHoras = BigDecimal.ZERO, totalExtra = BigDecimal.ZERO;
        BigDecimal totalBruto = BigDecimal.ZERO, totalDescontos = BigDecimal.ZERO, totalLiquido = BigDecimal.ZERO;

        for (Pagamento p : linhas) {
            sb.append(padDireita(p.nome(), 36)).append(' ')
                    .append(padEsquerda(Horas.formatar(p.composicao().horasNormais()), 5)).append(' ')
                    .append(padEsquerda(Horas.formatar(p.composicao().horasExtras()), 5)).append(' ')
                    .append(padEsquerda(Dinheiro.formatar(p.salarioBruto()), 13)).append(' ')
                    .append(padEsquerda(Dinheiro.formatar(p.descontos()), 9)).append(' ')
                    .append(padEsquerda(Dinheiro.formatar(p.salarioLiquido()), 15)).append(' ')
                    .append(p.metodoDescricao()).append('\n');
            totalHoras = totalHoras.add(p.composicao().horasNormais());
            totalExtra = totalExtra.add(p.composicao().horasExtras());
            totalBruto = totalBruto.add(p.salarioBruto());
            totalDescontos = totalDescontos.add(p.descontos());
            totalLiquido = totalLiquido.add(p.salarioLiquido());
        }

        sb.append('\n');
        sb.append(padDireita("TOTAL HORISTAS", 36)).append(' ')
                .append(padEsquerda(Horas.formatar(totalHoras), 5)).append(' ')
                .append(padEsquerda(Horas.formatar(totalExtra), 5)).append(' ')
                .append(padEsquerda(Dinheiro.formatar(totalBruto), 13)).append(' ')
                .append(padEsquerda(Dinheiro.formatar(totalDescontos), 9)).append(' ')
                .append(padEsquerda(Dinheiro.formatar(totalLiquido), 15)).append('\n');
        sb.append('\n');
        return sb.toString();
    }

    // ---- ASSALARIADOS: Nome(48) Bruto(13) Descontos(9) Liquido(15) Metodo ----

    private static String secaoAssalariados(List<Pagamento> linhas) {
        StringBuilder sb = new StringBuilder();
        sb.append(divisor()).append(tituloSecao("ASSALARIADOS")).append(divisor());
        sb.append(padDireita("Nome", 48)).append(' ')
                .append(padDireita("Salario Bruto", 13)).append(' ')
                .append(padDireita("Descontos", 9)).append(' ')
                .append(padDireita("Salario Liquido", 15)).append(' ')
                .append("Metodo").append('\n');
        sb.append(sublinhado(48, 13, 9, 15)).append('\n');

        BigDecimal totalBruto = BigDecimal.ZERO, totalDescontos = BigDecimal.ZERO, totalLiquido = BigDecimal.ZERO;

        for (Pagamento p : linhas) {
            sb.append(padDireita(p.nome(), 48)).append(' ')
                    .append(padEsquerda(Dinheiro.formatar(p.salarioBruto()), 13)).append(' ')
                    .append(padEsquerda(Dinheiro.formatar(p.descontos()), 9)).append(' ')
                    .append(padEsquerda(Dinheiro.formatar(p.salarioLiquido()), 15)).append(' ')
                    .append(p.metodoDescricao()).append('\n');
            totalBruto = totalBruto.add(p.salarioBruto());
            totalDescontos = totalDescontos.add(p.descontos());
            totalLiquido = totalLiquido.add(p.salarioLiquido());
        }

        sb.append('\n');
        sb.append(padDireita("TOTAL ASSALARIADOS", 48)).append(' ')
                .append(padEsquerda(Dinheiro.formatar(totalBruto), 13)).append(' ')
                .append(padEsquerda(Dinheiro.formatar(totalDescontos), 9)).append(' ')
                .append(padEsquerda(Dinheiro.formatar(totalLiquido), 15)).append('\n');
        sb.append('\n');
        return sb.toString();
    }

    // ---- COMISSIONADOS: Nome(21) Fixo(8) Vendas(8) Comissao(8) Bruto(13) Descontos(9) Liquido(15) Metodo ----

    private static String secaoComissionados(List<Pagamento> linhas) {
        StringBuilder sb = new StringBuilder();
        sb.append(divisor()).append(tituloSecao("COMISSIONADOS")).append(divisor());
        sb.append(padDireita("Nome", 21)).append(' ')
                .append(padDireita("Fixo", 8)).append(' ')
                .append(padDireita("Vendas", 8)).append(' ')
                .append(padDireita("Comissao", 8)).append(' ')
                .append(padDireita("Salario Bruto", 13)).append(' ')
                .append(padDireita("Descontos", 9)).append(' ')
                .append(padDireita("Salario Liquido", 15)).append(' ')
                .append("Metodo").append('\n');
        sb.append(sublinhado(21, 8, 8, 8, 13, 9, 15)).append('\n');

        BigDecimal totalFixo = BigDecimal.ZERO, totalVendas = BigDecimal.ZERO, totalComissao = BigDecimal.ZERO;
        BigDecimal totalBruto = BigDecimal.ZERO, totalDescontos = BigDecimal.ZERO, totalLiquido = BigDecimal.ZERO;

        for (Pagamento p : linhas) {
            sb.append(padDireita(p.nome(), 21)).append(' ')
                    .append(padEsquerda(Dinheiro.formatar(p.composicao().salarioFixo()), 8)).append(' ')
                    .append(padEsquerda(Dinheiro.formatar(p.composicao().vendas()), 8)).append(' ')
                    .append(padEsquerda(Dinheiro.formatar(p.composicao().comissao()), 8)).append(' ')
                    .append(padEsquerda(Dinheiro.formatar(p.salarioBruto()), 13)).append(' ')
                    .append(padEsquerda(Dinheiro.formatar(p.descontos()), 9)).append(' ')
                    .append(padEsquerda(Dinheiro.formatar(p.salarioLiquido()), 15)).append(' ')
                    .append(p.metodoDescricao()).append('\n');
            totalFixo = totalFixo.add(p.composicao().salarioFixo());
            totalVendas = totalVendas.add(p.composicao().vendas());
            totalComissao = totalComissao.add(p.composicao().comissao());
            totalBruto = totalBruto.add(p.salarioBruto());
            totalDescontos = totalDescontos.add(p.descontos());
            totalLiquido = totalLiquido.add(p.salarioLiquido());
        }

        sb.append('\n');
        sb.append(padDireita("TOTAL COMISSIONADOS", 21)).append(' ')
                .append(padEsquerda(Dinheiro.formatar(totalFixo), 8)).append(' ')
                .append(padEsquerda(Dinheiro.formatar(totalVendas), 8)).append(' ')
                .append(padEsquerda(Dinheiro.formatar(totalComissao), 8)).append(' ')
                .append(padEsquerda(Dinheiro.formatar(totalBruto), 13)).append(' ')
                .append(padEsquerda(Dinheiro.formatar(totalDescontos), 9)).append(' ')
                .append(padEsquerda(Dinheiro.formatar(totalLiquido), 15)).append('\n');
        sb.append('\n');
        return sb.toString();
    }

    // ---- pecas de formatacao reaproveitadas pelas 3 secoes ----

    private static String divisor() {
        return "=".repeat(LARGURA_TOTAL) + "\n";
    }

    private static String tituloSecao(String nome) {
        String prefixo = "=".repeat(21) + " " + nome + " ";
        return prefixo + "=".repeat(LARGURA_TOTAL - prefixo.length()) + "\n";
    }

    private static String sublinhado(int... larguras) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < larguras.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append("=".repeat(larguras[i]));
        }
        // calcula ANTES de acrescentar o espaco separador -- sb.append(' ') muda
        // sb.length(), e encadear os dois appends avaliaria o repeat() com o
        // tamanho ja errado (um a mais).
        int restante = LARGURA_TOTAL - sb.length() - 1;
        sb.append(' ').append("=".repeat(restante));
        return sb.toString();
    }

    private static String padDireita(String texto, int largura) {
        return String.format("%-" + largura + "s", texto);
    }

    private static String padEsquerda(String texto, int largura) {
        return String.format("%" + largura + "s", texto);
    }
}
