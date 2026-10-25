package br.ufal.ic.p2.wepayu.modelos;

import br.ufal.ic.p2.wepayu.modelos.agenda.AgendaPagamento;
import br.ufal.ic.p2.wepayu.util.Datas;
import br.ufal.ic.p2.wepayu.util.Periodo;

import java.math.BigDecimal;

/** Recebe um salario fixo mensal, dividido conforme a agenda de pagamento. */
public class EmpregadoAssalariado extends Empregado {

    private static final long serialVersionUID = 1L;

    public EmpregadoAssalariado(String id, String nome, String endereco, BigDecimal salarioMensal, AgendaPagamento agenda) {
        super(id, nome, endereco, salarioMensal, agenda, Datas.INICIO_DO_CALENDARIO);
    }

    @Override
    public TipoEmpregado getTipo() {
        return TipoEmpregado.ASSALARIADO;
    }

    @Override
    protected SalarioBruto calcularBruto(Periodo periodo) {
        return SalarioBruto.fixo(parcelaDoSalarioFixo());
    }
}
