package br.ufal.ic.p2.wepayu.modelos;

public class Correios extends MetodoPagamento {

    @Override
    public String getNome() {
        return "correios";
    }

    @Override
    public String getDescricao(String enderecoEmpregado) {
        return "Correios, " + enderecoEmpregado;
    }
}
