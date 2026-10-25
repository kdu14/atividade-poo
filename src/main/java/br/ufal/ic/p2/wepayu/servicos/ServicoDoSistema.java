package br.ufal.ic.p2.wepayu.servicos;

import br.ufal.ic.p2.wepayu.excecoes.WePayUException;

/** Comandos que valem para o sistema inteiro: zerar, encerrar, desfazer e refazer. */
public class ServicoDoSistema extends Servico {

    public ServicoDoSistema(Sessao sessao) {
        super(sessao);
    }

    public void zerarSistema() throws WePayUException {
        sessao.alterar(() -> sessao.dados().zerar());
    }

    public void encerrarSistema() throws WePayUException {
        sessao.encerrar();
    }

    public void undo() throws WePayUException {
        sessao.desfazer();
    }

    public void redo() throws WePayUException {
        sessao.refazer();
    }
}
