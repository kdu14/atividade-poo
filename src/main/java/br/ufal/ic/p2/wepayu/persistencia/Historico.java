package br.ufal.ic.p2.wepayu.persistencia;

import br.ufal.ic.p2.wepayu.excecoes.SemComandoParaDesfazerException;
import br.ufal.ic.p2.wepayu.excecoes.SemComandoParaRefazerException;
import br.ufal.ic.p2.wepayu.excecoes.WePayUException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Undo/redo por memento: antes de cada comando que muda o sistema, guarda
 * uma foto do repositorio. As pilhas nao vao para o disco -- cada execucao
 * do programa comeca sem historico, como a US8 espera.
 */
public class Historico {

    private final Repositorio repositorio;
    private final Deque<byte[]> paraDesfazer = new ArrayDeque<>();
    private final Deque<byte[]> paraRefazer = new ArrayDeque<>();

    public Historico(Repositorio repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Roda o comando como uma transacao: se ele falhar no meio, o
     * repositorio volta exatamente ao estado de antes e o erro segue para
     * quem chamou. Comando que falha nao entra no historico.
     */
    public <T> T executar(Transacao<T> transacao) throws WePayUException {
        byte[] antes = fotografar();
        try {
            T resultado = transacao.executar();
            paraDesfazer.push(antes);
            paraRefazer.clear();
            return resultado;
        } catch (WePayUException | RuntimeException e) {
            restaurar(antes);
            throw e;
        }
    }

    public void desfazer() throws SemComandoParaDesfazerException {
        if (paraDesfazer.isEmpty()) {
            throw new SemComandoParaDesfazerException();
        }
        paraRefazer.push(fotografar());
        restaurar(paraDesfazer.pop());
    }

    public void refazer() throws SemComandoParaRefazerException {
        if (paraRefazer.isEmpty()) {
            throw new SemComandoParaRefazerException();
        }
        paraDesfazer.push(fotografar());
        restaurar(paraRefazer.pop());
    }

    // falha aqui e de memoria, nao de disco: o estado e todo serializavel.
    private byte[] fotografar() {
        try {
            return Serializador.paraBytes(repositorio);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao fotografar o estado do sistema.", e);
        }
    }

    private void restaurar(byte[] foto) {
        try {
            repositorio.copiarDe(Serializador.deBytes(foto));
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao restaurar o estado do sistema.", e);
        }
    }
}
