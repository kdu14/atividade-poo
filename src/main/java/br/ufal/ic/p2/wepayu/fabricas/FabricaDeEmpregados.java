package br.ufal.ic.p2.wepayu.fabricas;

import br.ufal.ic.p2.wepayu.modelos.Empregado;
import br.ufal.ic.p2.wepayu.modelos.EmpregadoAssalariado;
import br.ufal.ic.p2.wepayu.modelos.EmpregadoComissionado;
import br.ufal.ic.p2.wepayu.modelos.EmpregadoHorista;
import br.ufal.ic.p2.wepayu.modelos.TipoEmpregado;

import java.math.BigDecimal;

/**
 * Unico lugar que sabe qual classe corresponde a cada tipo de empregado.
 * A validacao dos valores ja foi feita por quem chama; a comissao so e
 * usada para o comissionado.
 */
public final class FabricaDeEmpregados {

    private FabricaDeEmpregados() {
    }

    public static Empregado criar(String id, String nome, String endereco, TipoEmpregado tipo,
                                  BigDecimal salario, BigDecimal comissao) {
        return switch (tipo) {
            case HORISTA -> new EmpregadoHorista(id, nome, endereco, salario);
            case ASSALARIADO -> new EmpregadoAssalariado(id, nome, endereco, salario);
            case COMISSIONADO -> new EmpregadoComissionado(id, nome, endereco, salario, comissao);
        };
    }

    /**
     * Mesmo empregado com outro tipo: mantem identificacao, nome, endereco,
     * metodo de pagamento e filiacao ao sindicato (com as taxas lancadas).
     */
    public static Empregado converter(Empregado antigo, TipoEmpregado novoTipo, BigDecimal salario, BigDecimal comissao) {
        Empregado novo = criar(antigo.getId(), antigo.getNome(), antigo.getEndereco(), novoTipo, salario, comissao);
        antigo.transferirDadosComunsPara(novo);
        return novo;
    }
}
