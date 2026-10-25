package br.ufal.ic.p2.wepayu.fabricas;

import br.ufal.ic.p2.wepayu.modelos.Empregado;
import br.ufal.ic.p2.wepayu.modelos.EmpregadoAssalariado;
import br.ufal.ic.p2.wepayu.modelos.EmpregadoComissionado;
import br.ufal.ic.p2.wepayu.modelos.EmpregadoHorista;
import br.ufal.ic.p2.wepayu.modelos.TipoEmpregado;
import br.ufal.ic.p2.wepayu.modelos.agenda.AgendaPagamento;

import java.math.BigDecimal;

/**
 * Unico lugar que sabe qual classe corresponde a cada tipo de empregado.
 * A validacao dos valores ja foi feita por quem chama; a comissao so e
 * usada para o comissionado.
 */
public final class FabricaDeEmpregados {

    private FabricaDeEmpregados() {
    }

    /** Empregado novo, com a agenda padrao do tipo. */
    public static Empregado criar(String id, String nome, String endereco, TipoEmpregado tipo,
                                  BigDecimal salario, BigDecimal comissao) {
        return criar(id, nome, endereco, tipo, salario, comissao, FabricaDeAgendas.padraoDe(tipo));
    }

    /**
     * Mesmo empregado com outro tipo: mantem identificacao, nome, endereco,
     * metodo de pagamento e filiacao ao sindicato (com taxas e divida). A
     * agenda que o empregado escolheu tambem fica; se ele estava na agenda
     * padrao do tipo antigo, passa para a padrao do tipo novo.
     */
    public static Empregado converter(Empregado antigo, TipoEmpregado novoTipo, BigDecimal salario, BigDecimal comissao) {
        boolean usavaAgendaPadrao = antigo.getAgenda().equals(FabricaDeAgendas.padraoDe(antigo.getTipo()));
        AgendaPagamento agenda = usavaAgendaPadrao ? FabricaDeAgendas.padraoDe(novoTipo) : antigo.getAgenda();

        Empregado novo = criar(antigo.getId(), antigo.getNome(), antigo.getEndereco(), novoTipo, salario, comissao, agenda);
        antigo.transferirDadosComunsPara(novo);
        return novo;
    }

    private static Empregado criar(String id, String nome, String endereco, TipoEmpregado tipo,
                                   BigDecimal salario, BigDecimal comissao, AgendaPagamento agenda) {
        return switch (tipo) {
            case HORISTA -> new EmpregadoHorista(id, nome, endereco, salario, agenda);
            case ASSALARIADO -> new EmpregadoAssalariado(id, nome, endereco, salario, agenda);
            case COMISSIONADO -> new EmpregadoComissionado(id, nome, endereco, salario, comissao, agenda);
        };
    }
}
