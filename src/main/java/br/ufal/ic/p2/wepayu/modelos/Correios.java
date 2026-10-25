package br.ufal.ic.p2.wepayu.modelos;

public class Correios extends MetodoPagamento {

    private static final long serialVersionUID = 1L;

    @Override
    public String getNome() {
        return "correios";
    }

    @Override
    public String getDescricao(String enderecoEmpregado) {
        return "Correios, " + enderecoEmpregado;
    }
}
