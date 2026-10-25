package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.excecoes.WePayUException;
import br.ufal.ic.p2.wepayu.persistencia.ArquivoDeDados;
import br.ufal.ic.p2.wepayu.servicos.ServicoDaFolha;
import br.ufal.ic.p2.wepayu.servicos.ServicoDeAlteracao;
import br.ufal.ic.p2.wepayu.servicos.ServicoDeEmpregados;
import br.ufal.ic.p2.wepayu.servicos.ServicoDeLancamentos;
import br.ufal.ic.p2.wepayu.servicos.ServicoDoSindicato;
import br.ufal.ic.p2.wepayu.servicos.ServicoDoSistema;
import br.ufal.ic.p2.wepayu.servicos.Sessao;

/**
 * Fachada usada pelos testes de aceitacao (EasyAccept). Cada metodo publico
 * corresponde a um comando da linguagem de script e so repassa a chamada ao
 * servico do assunto correspondente; o construtor apenas monta os servicos.
 */
public class Facade {

    private static final String ARQUIVO_DE_DADOS = "wepayu.dat";

    private final ServicoDoSistema sistema;
    private final ServicoDeEmpregados empregados;
    private final ServicoDeAlteracao alteracao;
    private final ServicoDoSindicato sindicato;
    private final ServicoDeLancamentos lancamentos;
    private final ServicoDaFolha folha;

    public Facade() throws WePayUException {
        Sessao sessao = new Sessao(new ArquivoDeDados(ARQUIVO_DE_DADOS));
        sistema = new ServicoDoSistema(sessao);
        empregados = new ServicoDeEmpregados(sessao);
        sindicato = new ServicoDoSindicato(sessao);
        alteracao = new ServicoDeAlteracao(sessao, sindicato);
        lancamentos = new ServicoDeLancamentos(sessao);
        folha = new ServicoDaFolha(sessao);
    }

    // ---- sistema ----

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

    // ---- empregados ----

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws WePayUException {
        return empregados.criarEmpregado(nome, endereco, tipo, salario);
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao)
            throws WePayUException {
        return empregados.criarEmpregado(nome, endereco, tipo, salario, comissao);
    }

    public void removerEmpregado(String emp) throws WePayUException {
        empregados.removerEmpregado(emp);
    }

    public int getNumeroDeEmpregados() throws WePayUException {
        return empregados.getNumeroDeEmpregados();
    }

    public String getEmpregadoPorNome(String nome, String indice) throws WePayUException {
        return empregados.getEmpregadoPorNome(nome, indice);
    }

    public String getAtributoEmpregado(String emp, String atributo) throws WePayUException {
        return empregados.getAtributoEmpregado(emp, atributo);
    }

    // ---- alteracao ----

    public void alteraEmpregado(String emp, String atributo, String valor) throws WePayUException {
        alteracao.alteraEmpregado(emp, atributo, valor);
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String salarioOuComissao)
            throws WePayUException {
        alteracao.alteraEmpregado(emp, atributo, valor, salarioOuComissao);
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String idSindicato, String taxaSindical)
            throws WePayUException {
        alteracao.alteraEmpregado(emp, atributo, valor, idSindicato, taxaSindical);
    }

    public void alteraEmpregado(String emp, String atributo, String valor1, String banco, String agencia,
                                String contaCorrente) throws WePayUException {
        alteracao.alteraEmpregado(emp, atributo, valor1, banco, agencia, contaCorrente);
    }

    // ---- lancamentos e sindicato ----

    public void lancaCartao(String emp, String data, String horas) throws WePayUException {
        lancamentos.lancaCartao(emp, data, horas);
    }

    public void lancaVenda(String emp, String data, String valor) throws WePayUException {
        lancamentos.lancaVenda(emp, data, valor);
    }

    public void lancaTaxaServico(String membro, String data, String valor) throws WePayUException {
        sindicato.lancaTaxaServico(membro, data, valor);
    }

    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) throws WePayUException {
        return lancamentos.getHorasNormaisTrabalhadas(emp, dataInicial, dataFinal);
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) throws WePayUException {
        return lancamentos.getHorasExtrasTrabalhadas(emp, dataInicial, dataFinal);
    }

    public String getVendasRealizadas(String emp, String dataInicial, String dataFinal) throws WePayUException {
        return lancamentos.getVendasRealizadas(emp, dataInicial, dataFinal);
    }

    public String getTaxasServico(String emp, String dataInicial, String dataFinal) throws WePayUException {
        return sindicato.getTaxasServico(emp, dataInicial, dataFinal);
    }

    // ---- folha ----

    public String totalFolha(String data) throws WePayUException {
        return folha.totalFolha(data);
    }

    public void rodaFolha(String data, String saida) throws WePayUException {
        folha.rodaFolha(data, saida);
    }
}
