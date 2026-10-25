package br.ufal.ic.p2.wepayu.persistencia;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Converte o repositorio em bytes e de volta. Usado tanto pelo arquivo de
 * dados quanto pelas fotos do undo/redo -- as duas coisas guardam o mesmo
 * estado, do mesmo jeito.
 */
final class Serializador {

    private Serializador() {
    }

    static byte[] paraBytes(Repositorio repositorio) throws IOException {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ObjectOutputStream saida = new ObjectOutputStream(bytes)) {
            saida.writeObject(repositorio);
            saida.flush();
            return bytes.toByteArray();
        }
    }

    static Repositorio deBytes(byte[] bytes) throws IOException {
        try (ObjectInputStream entrada = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (Repositorio) entrada.readObject();
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IOException("Conteudo nao e um repositorio do WePayU.", e);
        }
    }
}
