package br.ufal.ic.p2.wepayu.persistencia;

import br.ufal.ic.p2.wepayu.excecoes.WePayUException;

/** Uma Transacao que nao devolve nada (a maioria dos comandos). */
@FunctionalInterface
public interface Alteracao {

    void executar() throws WePayUException;
}
