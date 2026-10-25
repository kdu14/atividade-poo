package br.ufal.ic.p2.wepayu.persistencia;

import br.ufal.ic.p2.wepayu.excecoes.ArquivoException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * O arquivo em disco onde o estado fica entre uma execucao e outra.
 * Serializacao Java comum, e nao XMLEncoder: o XMLEncoder exigiria o
 * padrao JavaBean (construtor vazio e setter publico para cada campo) em
 * toda a hierarquia, abrindo mao do encapsulamento.
 */
public class ArquivoDeDados {

    private final Path caminho;

    public ArquivoDeDados(String caminho) {
        this.caminho = Path.of(caminho);
    }

    /**
     * Le o estado gravado; sem arquivo, o sistema comeca vazio. Arquivo
     * ilegivel e erro (e nao um sistema vazio em silencio, que pareceria
     * perda de dados).
     */
    public Repositorio carregar() throws ArquivoException {
        if (!Files.exists(caminho)) {
            return new Repositorio();
        }
        try {
            return Serializador.deBytes(Files.readAllBytes(caminho));
        } catch (IOException e) {
            throw new ArquivoException("Nao foi possivel ler os dados gravados em " + caminho + ": " + e.getMessage());
        }
    }

    public void gravar(Repositorio repositorio) throws ArquivoException {
        try {
            Files.write(caminho, Serializador.paraBytes(repositorio));
        } catch (IOException e) {
            throw new ArquivoException("Nao foi possivel gravar a persistencia: " + e.getMessage());
        }
    }
}
