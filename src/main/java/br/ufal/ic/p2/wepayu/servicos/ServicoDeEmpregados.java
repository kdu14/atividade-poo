package br.ufal.ic.p2.wepayu.servicos;

import br.ufal.ic.p2.wepayu.excecoes.AtributoInexistenteException;
import br.ufal.ic.p2.wepayu.excecoes.EmpregadoNaoEncontradoPorNomeException;
import br.ufal.ic.p2.wepayu.excecoes.TipoNaoAplicavelException;
import br.ufal.ic.p2.wepayu.excecoes.WePayUException;
import br.ufal.ic.p2.wepayu.fabricas.FabricaDeEmpregados;
import br.ufal.ic.p2.wepayu.modelos.Empregado;
import br.ufal.ic.p2.wepayu.modelos.EmpregadoComissionado;
import br.ufal.ic.p2.wepayu.modelos.TipoEmpregado;
import br.ufal.ic.p2.wepayu.persistencia.Repositorio;
import br.ufal.ic.p2.wepayu.util.Dinheiro;
import br.ufal.ic.p2.wepayu.validacao.Entrada;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/** Cadastro de empregados: criar, remover e consultar. */
public class ServicoDeEmpregados extends Servico {

    /** Como ler cada atributo de getAtributoEmpregado. */
    @FunctionalInterface
    private interface LeitorDeAtributo {
        String ler(Empregado empregado) throws WePayUException;
    }

    private final Map<String, LeitorDeAtributo> leitores = new HashMap<>();

    public ServicoDeEmpregados(Sessao sessao) {
        super(sessao);
        leitores.put("nome", Empregado::getNome);
        leitores.put("endereco", Empregado::getEndereco);
        leitores.put("tipo", e -> e.getTipo().getNome());
        leitores.put("salario", e -> Dinheiro.formatar(e.getSalario()));
        leitores.put("comissao", e -> Dinheiro.formatar(EmpregadoComissionado.exigir(e).getComissao()));
        leitores.put("sindicalizado", e -> String.valueOf(e.isSindicalizado()));
        leitores.put("idSindicato", e -> e.getSindicato().getId());
        leitores.put("taxaSindical", e -> Dinheiro.formatar(e.getSindicato().getTaxaSindical()));
        leitores.put("metodoPagamento", e -> e.getMetodoPagamento().getNome());
        leitores.put("banco", e -> e.getMetodoPagamento().getBanco());
        leitores.put("agencia", e -> e.getMetodoPagamento().getAgencia());
        leitores.put("contaCorrente", e -> e.getMetodoPagamento().getContaCorrente());
        leitores.put("agendaPagamento", e -> e.getAgenda().getDescricao());
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws WePayUException {
        return criar(nome, endereco, tipo, salario, null);
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao)
            throws WePayUException {
        return criar(nome, endereco, tipo, salario, comissao);
    }

    public void removerEmpregado(String id) throws WePayUException {
        sessao.alterar(() -> sessao.dados().remover(buscarEmpregado(id).getId()));
    }

    public int getNumeroDeEmpregados() throws WePayUException {
        return sessao.dados().quantidade();
    }

    public String getEmpregadoPorNome(String nome, String indiceTexto) throws WePayUException {
        int indice = Entrada.indice(indiceTexto);
        if (nome == null) {
            throw new EmpregadoNaoEncontradoPorNomeException();
        }
        int encontrados = 0;
        for (Empregado empregado : sessao.dados().listar()) {
            if (empregado.getNome().contains(nome) && ++encontrados == indice) {
                return empregado.getId();
            }
        }
        throw new EmpregadoNaoEncontradoPorNomeException();
    }

    public String getAtributoEmpregado(String id, String atributo) throws WePayUException {
        Empregado empregado = buscarEmpregado(id);
        LeitorDeAtributo leitor = leitores.get(atributo);
        if (leitor == null) {
            throw new AtributoInexistenteException();
        }
        return leitor.ler(empregado);
    }

    // comissao == null significa "comando sem comissao": so serve para quem nao e comissionado.
    private String criar(String nome, String endereco, String tipoTexto, String salarioTexto, String comissaoTexto)
            throws WePayUException {
        return sessao.executar(() -> {
            Entrada.texto(nome, "Nome nao pode ser nulo.");
            Entrada.texto(endereco, "Endereco nao pode ser nulo.");
            TipoEmpregado tipo = TipoEmpregado.porNome(tipoTexto);
            if ((tipo == TipoEmpregado.COMISSIONADO) != (comissaoTexto != null)) {
                throw new TipoNaoAplicavelException();
            }
            BigDecimal salario = Entrada.salario(salarioTexto);
            BigDecimal comissao = (comissaoTexto == null) ? null : Entrada.comissao(comissaoTexto);

            // o id so e consumido depois de tudo validado
            Repositorio dados = sessao.dados();
            Empregado empregado = FabricaDeEmpregados.criar(dados.gerarId(), nome, endereco, tipo, salario, comissao);
            dados.salvar(empregado);
            return empregado.getId();
        });
    }
}
