package br.ufal.ic.p2.wepayu.servicos;

import br.ufal.ic.p2.wepayu.excecoes.AtributoInexistenteException;
import br.ufal.ic.p2.wepayu.excecoes.CampoObrigatorioException;
import br.ufal.ic.p2.wepayu.excecoes.MetodoPagamentoInvalidoException;
import br.ufal.ic.p2.wepayu.excecoes.TipoNaoAplicavelException;
import br.ufal.ic.p2.wepayu.excecoes.WePayUException;
import br.ufal.ic.p2.wepayu.fabricas.FabricaDeEmpregados;
import br.ufal.ic.p2.wepayu.modelos.Correios;
import br.ufal.ic.p2.wepayu.modelos.EmBanco;
import br.ufal.ic.p2.wepayu.modelos.EmMaos;
import br.ufal.ic.p2.wepayu.modelos.Empregado;
import br.ufal.ic.p2.wepayu.modelos.EmpregadoComissionado;
import br.ufal.ic.p2.wepayu.modelos.TipoEmpregado;
import br.ufal.ic.p2.wepayu.validacao.Entrada;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/** As variacoes de alteraEmpregado. */
public class ServicoDeAlteracao extends Servico {

    /** Como alterar cada atributo na forma simples (emp, atributo, valor). */
    @FunctionalInterface
    private interface AlteradorDeAtributo {
        void alterar(Empregado empregado, String valor) throws WePayUException;
    }

    private final ServicoDoSindicato sindicato;
    private final Map<String, AlteradorDeAtributo> alteradores = new HashMap<>();

    public ServicoDeAlteracao(Sessao sessao, ServicoDoSindicato sindicato) {
        super(sessao);
        this.sindicato = sindicato;
        alteradores.put("nome", (e, valor) -> e.setNome(Entrada.texto(valor, "Nome nao pode ser nulo.")));
        alteradores.put("endereco", (e, valor) -> e.setEndereco(Entrada.texto(valor, "Endereco nao pode ser nulo.")));
        alteradores.put("salario", (e, valor) -> e.setSalario(Entrada.salario(valor)));
        alteradores.put("comissao", (e, valor) -> EmpregadoComissionado.exigir(e).setComissao(Entrada.comissao(valor)));
        alteradores.put("tipo", (e, valor) -> mudarTipo(e, valor, null));
        alteradores.put("sindicalizado", this::alterarSindicalizado);
        alteradores.put("metodoPagamento", this::alterarMetodoPagamento);
        alteradores.put("agendaPagamento", (e, valor) -> e.setAgenda(sessao.dados().agendaDisponivel(valor)));
    }

    public void alteraEmpregado(String id, String atributo, String valor) throws WePayUException {
        sessao.alterar(() -> {
            Empregado empregado = buscarEmpregado(id);
            AlteradorDeAtributo alterador = alteradores.get(atributo);
            if (alterador == null) {
                throw new AtributoInexistenteException();
            }
            alterador.alterar(empregado, valor);
        });
    }

    /** Troca de tipo informando o salario (horista/assalariado) ou a comissao (comissionado). */
    public void alteraEmpregado(String id, String atributo, String valor, String salarioOuComissao) throws WePayUException {
        sessao.alterar(() -> {
            Empregado empregado = buscarEmpregado(id);
            exigirAtributo(atributo, "tipo");
            mudarTipo(empregado, valor, salarioOuComissao);
        });
    }

    public void alteraEmpregado(String id, String atributo, String valor, String idSindicato, String taxaSindical)
            throws WePayUException {
        sessao.alterar(() -> {
            Empregado empregado = buscarEmpregado(id);
            exigirAtributo(atributo, "sindicalizado");
            sindicato.sindicalizar(empregado, idSindicato, taxaSindical);
        });
    }

    public void alteraEmpregado(String id, String atributo, String valor, String banco, String agencia,
                                String contaCorrente) throws WePayUException {
        sessao.alterar(() -> {
            Empregado empregado = buscarEmpregado(id);
            exigirAtributo(atributo, "metodoPagamento");
            empregado.setMetodoPagamento(new EmBanco(
                    Entrada.texto(banco, "Banco nao pode ser nulo."),
                    Entrada.texto(agencia, "Agencia nao pode ser nulo."),
                    Entrada.texto(contaCorrente, "Conta corrente nao pode ser nulo.")));
        });
    }

    private void exigirAtributo(String atributo, String esperado) throws AtributoInexistenteException {
        if (!esperado.equals(atributo)) {
            throw new AtributoInexistenteException();
        }
    }

    // a forma simples so desfilia; filiar exige id e taxa (outra variacao do comando).
    private void alterarSindicalizado(Empregado empregado, String valor) throws WePayUException {
        if (Entrada.booleano(valor)) {
            throw new CampoObrigatorioException("Identificacao do sindicato nao pode ser nula.");
        }
        sindicato.dessindicalizar(empregado);
    }

    // banco tem variacao propria do comando, com agencia e conta.
    private void alterarMetodoPagamento(Empregado empregado, String valor) throws WePayUException {
        switch (valor == null ? "" : valor) {
            case "correios" -> empregado.setMetodoPagamento(new Correios());
            case "emMaos" -> empregado.setMetodoPagamento(new EmMaos());
            default -> throw new MetodoPagamentoInvalidoException();
        }
    }

    /**
     * Para comissionado o extra (comissao) e obrigatorio; para os outros e
     * opcional e, se vier, e o novo salario (senao fica o de antes).
     */
    private void mudarTipo(Empregado empregado, String tipoTexto, String extra) throws WePayUException {
        TipoEmpregado novoTipo = TipoEmpregado.porNome(tipoTexto);
        BigDecimal salario = empregado.getSalario();
        BigDecimal comissao = null;
        if (novoTipo == TipoEmpregado.COMISSIONADO) {
            if (extra == null) {
                throw new TipoNaoAplicavelException();
            }
            comissao = Entrada.comissao(extra);
        } else if (extra != null) {
            salario = Entrada.salario(extra);
        }
        sessao.dados().salvar(FabricaDeEmpregados.converter(empregado, novoTipo, salario, comissao));
    }
}
