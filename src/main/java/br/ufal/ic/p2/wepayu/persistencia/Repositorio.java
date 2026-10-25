package br.ufal.ic.p2.wepayu.persistencia;

import br.ufal.ic.p2.wepayu.excecoes.AgendaIndisponivelException;
import br.ufal.ic.p2.wepayu.excecoes.EmpregadoNaoExisteException;
import br.ufal.ic.p2.wepayu.fabricas.FabricaDeAgendas;
import br.ufal.ic.p2.wepayu.modelos.Empregado;
import br.ufal.ic.p2.wepayu.modelos.agenda.AgendaPagamento;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Todo o estado do sistema: os empregados, o indice de identificacoes do
 * sindicato, o contador de ids e as agendas de pagamento disponiveis. E
 * este objeto inteiro que vai para o arquivo de dados e para as fotos do
 * undo/redo.
 */
public class Repositorio implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Empregado> empregados = new LinkedHashMap<>();
    private Map<String, String> empregadoPorIdSindicato = new LinkedHashMap<>();
    private int proximoId = 1;
    private Map<String, AgendaPagamento> agendasDisponiveis = agendasPadrao();

    public Empregado buscar(String id) throws EmpregadoNaoExisteException {
        Empregado empregado = empregados.get(id);
        if (empregado == null) {
            throw new EmpregadoNaoExisteException();
        }
        return empregado;
    }

    /** Somente leitura: alteracoes passam pelos metodos deste repositorio. */
    public Collection<Empregado> listar() {
        return Collections.unmodifiableCollection(empregados.values());
    }

    public int quantidade() {
        return empregados.size();
    }

    /** Consome um id; quem chama so deve fazer isto depois de validar tudo. */
    public String gerarId() {
        return String.valueOf(proximoId++);
    }

    /** Inclui um empregado novo ou substitui o de mesmo id (troca de tipo). */
    public void salvar(Empregado empregado) {
        empregados.put(empregado.getId(), empregado);
    }

    public void remover(String id) {
        empregados.remove(id);
        empregadoPorIdSindicato.values().remove(id);
    }

    public Optional<String> empregadoComIdSindicato(String idSindicato) {
        return Optional.ofNullable(empregadoPorIdSindicato.get(idSindicato));
    }

    /** Mantem no indice so a identificacao atual do empregado (ou nenhuma, se for null). */
    public void atualizarIdSindicato(String idEmpregado, String idSindicato) {
        empregadoPorIdSindicato.values().remove(idEmpregado);
        if (idSindicato != null) {
            empregadoPorIdSindicato.put(idSindicato, idEmpregado);
        }
    }

    public AgendaPagamento agendaDisponivel(String descricao) throws AgendaIndisponivelException {
        AgendaPagamento agenda = agendasDisponiveis.get(descricao);
        if (agenda == null) {
            throw new AgendaIndisponivelException();
        }
        return agenda;
    }

    public void zerar() {
        copiarDe(new Repositorio());
    }

    /** Passa a ter exatamente o conteudo de outro repositorio (restaurar foto, carregar arquivo). */
    public void copiarDe(Repositorio outro) {
        empregados = outro.empregados;
        empregadoPorIdSindicato = outro.empregadoPorIdSindicato;
        proximoId = outro.proximoId;
        agendasDisponiveis = outro.agendasDisponiveis;
    }

    private static Map<String, AgendaPagamento> agendasPadrao() {
        Map<String, AgendaPagamento> agendas = new LinkedHashMap<>();
        for (AgendaPagamento agenda : FabricaDeAgendas.padroes()) {
            agendas.put(agenda.getDescricao(), agenda);
        }
        return agendas;
    }
}
