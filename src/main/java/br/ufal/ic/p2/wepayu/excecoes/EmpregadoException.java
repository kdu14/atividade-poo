package br.ufal.ic.p2.wepayu.excecoes;

// unica excecao de negocio do sistema -- o EasyAccept so confere o texto de
// getMessage(), entao nao precisa de uma classe diferente por tipo de erro.
public class EmpregadoException extends Exception {

    public EmpregadoException(String mensagem) {
        super(mensagem);
    }
}
