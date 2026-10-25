package br.ufal.ic.p2.wepayu;

import easyaccept.EasyAccept;

import java.io.File;

/**
 * Roda os testes de aceitacao (User Stories 1 a 10) usando o EasyAccept,
 * e no fim os testes extras do Milestone 2 (tests/extras, escritos por nos).
 * Cada chamada abaixo cria uma nova Facade (como se fosse um processo Java
 * separado) -- e assim que a persistencia em disco entre um script e o seu
 * par de continuacao "_1" e realmente testada.
 */
public class Main {
    public static void main(String[] args) {
        String facade = "br.ufal.ic.p2.wepayu.Facade";

        String[] scripts = {
                "tests/us1.txt",
                "tests/us1_1.txt",
                "tests/us2.txt",
                "tests/us2_1.txt",
                "tests/us3.txt",
                "tests/us3_1.txt",
                "tests/us4.txt",
                "tests/us4_1.txt",
                "tests/us5.txt",
                "tests/us5_1.txt",
                "tests/us6.txt",
                "tests/us6_1.txt",
                "tests/us7.txt",
                "tests/us8.txt",
                "tests/us9.txt",
                "tests/us9_1.txt",
                "tests/us10.txt",
                "tests/us10_1.txt",
                "tests/extras/agendas.txt"
        };

        // comeca de um estado limpo: um wepayu.dat que sobrou de uma execucao
        // anterior faria o primeiro script ler dados que ele nao criou.
        new File("wepayu.dat").delete();

        // "quit", no fim de cada script, encerra a chamada inteira ao EasyAccept
        // -- por isso cada arquivo precisa da sua PROPRIA chamada a main().
        for (String script : scripts) {
            EasyAccept.main(new String[]{facade, script});
        }
    }
}
