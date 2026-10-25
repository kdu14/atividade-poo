package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.excecoes.WePayUException;

/**
 * Fachada usada pelos testes de aceitacao (EasyAccept). Cada metodo publico
 * corresponde a um comando da linguagem de script e so repassa a chamada
 * pra Sistema, que concentra toda a logica de negocio.
 */
public class Facade {

    private final Sistema sistema = new Sistema();

    public void zerarSistema() throws WePayUException {
        sistema.zerarSistema();
    }

    public void encerrarSistema() throws WePayUException {
        sistema.encerrarSistema();
    }

    public void undo() throws WePayUException {
        sistema.undo();
    }

    public void redo() throws WePayUException {
        sistema.redo();
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws WePayUException {
        return sistema.criarEmpregado(nome, endereco, tipo, salario);
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws WePayUException {
        return sistema.criarEmpregado(nome, endereco, tipo, salario, comissao);
    }

    public void removerEmpregado(String emp) throws WePayUException {
        sistema.removerEmpregado(emp);
    }

    public int getNumeroDeEmpregados() throws WePayUException {
        return sistema.getNumeroDeEmpregados();
    }

    public String getEmpregadoPorNome(String nome, String indice) throws WePayUException {
        return sistema.getEmpregadoPorNome(nome, indice);
    }

    public String getAtributoEmpregado(String emp, String atributo) throws WePayUException {
        return sistema.getAtributoEmpregado(emp, atributo);
    }

    public void alteraEmpregado(String emp, String atributo, String valor) throws WePayUException {
        sistema.alteraEmpregado(emp, atributo, valor);
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String extra) throws WePayUException {
        sistema.alteraEmpregado(emp, atributo, valor, extra);
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String idSindicato, String taxaSindical) throws WePayUException {
        sistema.alteraEmpregado(emp, atributo, valor, idSindicato, taxaSindical);
    }

    public void alteraEmpregado(String emp, String atributo, String valor1, String banco, String agencia, String contaCorrente) throws WePayUException {
        sistema.alteraEmpregado(emp, atributo, valor1, banco, agencia, contaCorrente);
    }

    public void lancaCartao(String emp, String data, String horas) throws WePayUException {
        sistema.lancaCartao(emp, data, horas);
    }

    public void lancaVenda(String emp, String data, String valor) throws WePayUException {
        sistema.lancaVenda(emp, data, valor);
    }

    public void lancaTaxaServico(String membro, String data, String valor) throws WePayUException {
        sistema.lancaTaxaServico(membro, data, valor);
    }

    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) throws WePayUException {
        return sistema.getHorasNormaisTrabalhadas(emp, dataInicial, dataFinal);
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) throws WePayUException {
        return sistema.getHorasExtrasTrabalhadas(emp, dataInicial, dataFinal);
    }

    public String getVendasRealizadas(String emp, String dataInicial, String dataFinal) throws WePayUException {
        return sistema.getVendasRealizadas(emp, dataInicial, dataFinal);
    }

    public String getTaxasServico(String emp, String dataInicial, String dataFinal) throws WePayUException {
        return sistema.getTaxasServico(emp, dataInicial, dataFinal);
    }

    public String totalFolha(String data) throws WePayUException {
        return sistema.totalFolha(data);
    }

    public void rodaFolha(String data, String saida) throws WePayUException {
        sistema.rodaFolha(data, saida);
    }
}
