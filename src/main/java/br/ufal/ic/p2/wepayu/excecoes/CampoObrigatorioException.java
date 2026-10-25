package br.ufal.ic.p2.wepayu.excecoes;

/** Um campo obrigatorio veio vazio. A mensagem diz qual (ex.: "Nome nao pode ser nulo."). */
public class CampoObrigatorioException extends WePayUException {

    private static final long serialVersionUID = 1L;

    public CampoObrigatorioException(String mensagem) {
        super(mensagem);
    }
}
