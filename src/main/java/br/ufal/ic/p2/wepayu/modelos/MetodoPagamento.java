package br.ufal.ic.p2.wepayu.modelos;

import br.ufal.ic.p2.wepayu.excecoes.EmpregadoNaoRecebeEmBancoException;

import java.io.Serializable;

/** Como um empregado recebe o salario: cada forma e uma subclasse que sabe se descrever sozinha. */
public abstract class MetodoPagamento implements Serializable {

    private static final long serialVersionUID = 1L;

    public abstract String getNome();

    /** A descricao usada na coluna "Metodo" do relatorio de folha. */
    public abstract String getDescricao(String enderecoEmpregado);

    // so EmBanco sobrescreve pra true -- evita instanceof pra saber o tipo.
    public boolean recebeEmBanco() {
        return false;
    }

    public String getBanco() throws EmpregadoNaoRecebeEmBancoException {
        throw new EmpregadoNaoRecebeEmBancoException();
    }

    public String getAgencia() throws EmpregadoNaoRecebeEmBancoException {
        throw new EmpregadoNaoRecebeEmBancoException();
    }

    public String getContaCorrente() throws EmpregadoNaoRecebeEmBancoException {
        throw new EmpregadoNaoRecebeEmBancoException();
    }
}
