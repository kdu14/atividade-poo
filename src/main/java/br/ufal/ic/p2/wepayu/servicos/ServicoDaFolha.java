package br.ufal.ic.p2.wepayu.servicos;

import br.ufal.ic.p2.wepayu.excecoes.ArquivoException;
import br.ufal.ic.p2.wepayu.excecoes.WePayUException;
import br.ufal.ic.p2.wepayu.modelos.Empregado;
import br.ufal.ic.p2.wepayu.modelos.Pagamento;
import br.ufal.ic.p2.wepayu.relatorio.RelatorioDaFolha;
import br.ufal.ic.p2.wepayu.util.Dinheiro;
import br.ufal.ic.p2.wepayu.validacao.Entrada;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** Folha de pagamento: o total de um dia e o relatorio com o pagamento de cada empregado. */
public class ServicoDaFolha extends Servico {

    public ServicoDaFolha(Sessao sessao) {
        super(sessao);
    }

    public String totalFolha(String dataTexto) throws WePayUException {
        LocalDate data = Entrada.data(dataTexto, "Data invalida.");
        BigDecimal total = BigDecimal.ZERO;
        for (Pagamento pagamento : calcularPagamentos(data).values()) {
            total = total.add(pagamento.salarioBruto());
        }
        return Dinheiro.formatar(total);
    }

    /**
     * Grava o relatorio e so depois marca os empregados como pagos: se a
     * gravacao falhar, ninguem fica pago sem relatorio (e a transacao desfaz o resto).
     */
    public void rodaFolha(String dataTexto, String arquivoSaida) throws WePayUException {
        sessao.alterar(() -> {
            LocalDate data = Entrada.data(dataTexto, "Data invalida.");
            Map<Empregado, Pagamento> pagamentos = calcularPagamentos(data);
            gravar(arquivoSaida, RelatorioDaFolha.montar(data, pagamentos.values()));
            pagamentos.forEach((empregado, pagamento) -> empregado.confirmarPagamento(pagamento, data));
        });
    }

    private Map<Empregado, Pagamento> calcularPagamentos(LocalDate data) throws WePayUException {
        Map<Empregado, Pagamento> pagamentos = new LinkedHashMap<>();
        for (Empregado empregado : sessao.dados().listar()) {
            if (empregado.estaNaDataDePagamento(data)) {
                pagamentos.put(empregado, empregado.calcularPagamento(data));
            }
        }
        return pagamentos;
    }

    private void gravar(String caminho, String conteudo) throws ArquivoException {
        try {
            Files.writeString(Path.of(caminho), conteudo, StandardCharsets.UTF_8);
        } catch (IOException | RuntimeException e) {
            throw new ArquivoException("Nao foi possivel escrever o arquivo de saida: " + e.getMessage());
        }
    }
}
