package br.ufal.ic.p2.wepayu.servicos;

import br.ufal.ic.p2.wepayu.excecoes.WePayUException;
import br.ufal.ic.p2.wepayu.modelos.Empregado;
import br.ufal.ic.p2.wepayu.validacao.Entrada;

/** O que todo servico compartilha: a sessao e a busca de empregado pelo id do comando. */
abstract class Servico {

    protected final Sessao sessao;

    protected Servico(Sessao sessao) {
        this.sessao = sessao;
    }

    protected Empregado buscarEmpregado(String id) throws WePayUException {
        Entrada.texto(id, "Identificacao do empregado nao pode ser nula.");
        return sessao.dados().buscar(id);
    }
}
