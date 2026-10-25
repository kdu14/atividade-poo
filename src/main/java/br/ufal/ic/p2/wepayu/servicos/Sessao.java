package br.ufal.ic.p2.wepayu.servicos;

import br.ufal.ic.p2.wepayu.excecoes.SistemaEncerradoException;
import br.ufal.ic.p2.wepayu.excecoes.WePayUException;
import br.ufal.ic.p2.wepayu.persistencia.Alteracao;
import br.ufal.ic.p2.wepayu.persistencia.ArquivoDeDados;
import br.ufal.ic.p2.wepayu.persistencia.Historico;
import br.ufal.ic.p2.wepayu.persistencia.Repositorio;
import br.ufal.ic.p2.wepayu.persistencia.Transacao;

/**
 * O ciclo de vida de uma execucao do sistema, compartilhado pelos servicos.
 * Todo acesso ao estado passa por aqui -- e por isso a regra "nada depois
 * de encerrarSistema" e conferida num lugar so.
 */
public class Sessao {

    private final ArquivoDeDados arquivo;
    private final Repositorio repositorio;
    private final Historico historico;
    private boolean encerrada = false;

    public Sessao(ArquivoDeDados arquivo) throws WePayUException {
        this.arquivo = arquivo;
        this.repositorio = arquivo.carregar();
        this.historico = new Historico(repositorio);
    }

    /** Para consultas: o estado atual, sem entrar no historico. */
    public Repositorio dados() throws SistemaEncerradoException {
        verificarAberta();
        return repositorio;
    }

    /** Para comandos que alteram o estado: transacao com undo/redo (ver Historico). */
    public <T> T executar(Transacao<T> transacao) throws WePayUException {
        verificarAberta();
        return historico.executar(transacao);
    }

    /** Mesmo que executar, para comandos que nao devolvem valor. */
    public void alterar(Alteracao alteracao) throws WePayUException {
        executar(() -> {
            alteracao.executar();
            return null;
        });
    }

    public void desfazer() throws WePayUException {
        verificarAberta();
        historico.desfazer();
    }

    public void refazer() throws WePayUException {
        verificarAberta();
        historico.refazer();
    }

    public void encerrar() throws WePayUException {
        verificarAberta();
        arquivo.gravar(repositorio);
        encerrada = true;
    }

    private void verificarAberta() throws SistemaEncerradoException {
        if (encerrada) {
            throw new SistemaEncerradoException();
        }
    }
}
