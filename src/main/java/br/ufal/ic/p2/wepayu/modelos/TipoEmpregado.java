package br.ufal.ic.p2.wepayu.modelos;

import br.ufal.ic.p2.wepayu.excecoes.TipoInvalidoException;

/** Os tipos de empregado, com o nome usado na linguagem de script. */
public enum TipoEmpregado {

    HORISTA("horista"),
    ASSALARIADO("assalariado"),
    COMISSIONADO("comissionado");

    private final String nome;

    TipoEmpregado(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public static TipoEmpregado porNome(String nome) throws TipoInvalidoException {
        for (TipoEmpregado tipo : values()) {
            if (tipo.nome.equals(nome)) {
                return tipo;
            }
        }
        throw new TipoInvalidoException();
    }
}
