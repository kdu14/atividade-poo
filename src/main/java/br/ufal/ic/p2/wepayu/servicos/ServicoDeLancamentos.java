package br.ufal.ic.p2.wepayu.servicos;

import br.ufal.ic.p2.wepayu.excecoes.WePayUException;
import br.ufal.ic.p2.wepayu.modelos.CartaoPonto;
import br.ufal.ic.p2.wepayu.modelos.Empregado;
import br.ufal.ic.p2.wepayu.modelos.EmpregadoComissionado;
import br.ufal.ic.p2.wepayu.modelos.EmpregadoHorista;
import br.ufal.ic.p2.wepayu.modelos.ResultadoVenda;
import br.ufal.ic.p2.wepayu.util.Dinheiro;
import br.ufal.ic.p2.wepayu.util.Horas;
import br.ufal.ic.p2.wepayu.util.Periodo;
import br.ufal.ic.p2.wepayu.validacao.Entrada;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Cartoes de ponto (horista) e resultados de venda (comissionado). */
public class ServicoDeLancamentos extends Servico {

    public ServicoDeLancamentos(Sessao sessao) {
        super(sessao);
    }

    public void lancaCartao(String id, String dataTexto, String horasTexto) throws WePayUException {
        sessao.alterar(() -> {
            Empregado empregado = buscarEmpregado(id);
            LocalDate data = Entrada.data(dataTexto, "Data invalida.");
            BigDecimal horas = Entrada.positivo(horasTexto, "Horas devem ser positivas.");
            EmpregadoHorista.exigir(empregado).lancarCartao(new CartaoPonto(data, horas));
        });
    }

    public void lancaVenda(String id, String dataTexto, String valorTexto) throws WePayUException {
        sessao.alterar(() -> {
            Empregado empregado = buscarEmpregado(id);
            LocalDate data = Entrada.data(dataTexto, "Data invalida.");
            BigDecimal valor = Entrada.positivo(valorTexto, "Valor deve ser positivo.");
            EmpregadoComissionado.exigir(empregado).lancarVenda(new ResultadoVenda(data, valor));
        });
    }

    public String getHorasNormaisTrabalhadas(String id, String dataInicial, String dataFinal) throws WePayUException {
        Empregado empregado = buscarEmpregado(id);
        Periodo periodo = Entrada.periodo(dataInicial, dataFinal);
        return Horas.formatar(EmpregadoHorista.exigir(empregado).getHorasNormaisNoPeriodo(periodo));
    }

    public String getHorasExtrasTrabalhadas(String id, String dataInicial, String dataFinal) throws WePayUException {
        Empregado empregado = buscarEmpregado(id);
        Periodo periodo = Entrada.periodo(dataInicial, dataFinal);
        return Horas.formatar(EmpregadoHorista.exigir(empregado).getHorasExtrasNoPeriodo(periodo));
    }

    public String getVendasRealizadas(String id, String dataInicial, String dataFinal) throws WePayUException {
        Empregado empregado = buscarEmpregado(id);
        Periodo periodo = Entrada.periodo(dataInicial, dataFinal);
        return Dinheiro.formatar(EmpregadoComissionado.exigir(empregado).getVendasNoPeriodo(periodo));
    }
}
