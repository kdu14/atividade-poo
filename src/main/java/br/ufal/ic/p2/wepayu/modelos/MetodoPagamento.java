package br.ufal.ic.p2.wepayu.modelos;

import java.io.Serializable;

/** Como um empregado recebe o salario: cada forma e uma subclasse que sabe se descrever sozinha. */
public abstract class MetodoPagamento implements Serializable {

    public abstract String getNome();

    /** A descricao usada na coluna "Metodo" do relatorio de folha. */
    public abstract String getDescricao(String enderecoEmpregado);

    // so EmBanco sobrescreve pra true -- evita instanceof pra saber o tipo.
    public boolean recebeEmBanco() {
        return false;
    }

    public String getBanco() {
        throw new UnsupportedOperationException("Empregado nao recebe em banco.");
    }

    public String getAgencia() {
        throw new UnsupportedOperationException("Empregado nao recebe em banco.");
    }

    public String getContaCorrente() {
        throw new UnsupportedOperationException("Empregado nao recebe em banco.");
    }
}
