package br.ufal.ic.p2.wepayu.persistencia;

import br.ufal.ic.p2.wepayu.excecoes.WePayUException;

/** Um comando que altera o repositorio e pode falhar com erro de negocio. */
@FunctionalInterface
public interface Transacao<T> {

    T executar() throws WePayUException;
}
