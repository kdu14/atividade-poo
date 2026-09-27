package br.ufal.ic.p2.wepayu.modelos;

public class EmMaos extends MetodoPagamento {

    @Override
    public String getNome() {
        return "emMaos";
    }

    @Override
    public String getDescricao(String enderecoEmpregado) {
        return "Em maos";
    }
}
