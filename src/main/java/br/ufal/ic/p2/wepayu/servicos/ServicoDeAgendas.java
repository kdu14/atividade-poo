package br.ufal.ic.p2.wepayu.servicos;

import br.ufal.ic.p2.wepayu.excecoes.WePayUException;
import br.ufal.ic.p2.wepayu.fabricas.FabricaDeAgendas;

/** Agendas de pagamento que a empresa disponibiliza para os empregados escolherem. */
public class ServicoDeAgendas extends Servico {

    public ServicoDeAgendas(Sessao sessao) {
        super(sessao);
    }

    public void criarAgendaDePagamentos(String descricao) throws WePayUException {
        sessao.alterar(() -> sessao.dados().adicionarAgenda(FabricaDeAgendas.aPartirDe(descricao)));
    }
}
