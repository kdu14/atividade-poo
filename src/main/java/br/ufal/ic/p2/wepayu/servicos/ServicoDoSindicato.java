package br.ufal.ic.p2.wepayu.servicos;

import br.ufal.ic.p2.wepayu.excecoes.IdSindicatoDuplicadoException;
import br.ufal.ic.p2.wepayu.excecoes.MembroNaoExisteException;
import br.ufal.ic.p2.wepayu.excecoes.WePayUException;
import br.ufal.ic.p2.wepayu.modelos.Empregado;
import br.ufal.ic.p2.wepayu.modelos.TaxaServico;
import br.ufal.ic.p2.wepayu.persistencia.Repositorio;
import br.ufal.ic.p2.wepayu.util.Dinheiro;
import br.ufal.ic.p2.wepayu.validacao.Entrada;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Filiacao ao sindicato e taxas de servico. */
public class ServicoDoSindicato extends Servico {

    public ServicoDoSindicato(Sessao sessao) {
        super(sessao);
    }

    public void lancaTaxaServico(String idMembro, String dataTexto, String valorTexto) throws WePayUException {
        sessao.alterar(() -> {
            Entrada.texto(idMembro, "Identificacao do membro nao pode ser nula.");
            String idEmpregado = sessao.dados().empregadoComIdSindicato(idMembro)
                    .orElseThrow(MembroNaoExisteException::new);
            LocalDate data = Entrada.data(dataTexto, "Data invalida.");
            BigDecimal valor = Entrada.positivo(valorTexto, "Valor deve ser positivo.");
            sessao.dados().buscar(idEmpregado).getSindicato().lancarTaxaServico(new TaxaServico(data, valor));
        });
    }

    public String getTaxasServico(String id, String dataInicial, String dataFinal) throws WePayUException {
        Empregado empregado = buscarEmpregado(id);
        BigDecimal taxa = empregado.getSindicato().getTaxasServicoNoPeriodo(Entrada.periodo(dataInicial, dataFinal));
        return Dinheiro.formatar(taxa);
    }

    /** Parte de alteraEmpregado: chamado dentro da transacao do comando. */
    void sindicalizar(Empregado empregado, String idSindicato, String taxaSindicalTexto) throws WePayUException {
        Entrada.texto(idSindicato, "Identificacao do sindicato nao pode ser nula.");
        BigDecimal taxaSindical = Entrada.taxaSindical(taxaSindicalTexto);

        Repositorio dados = sessao.dados();
        String dono = dados.empregadoComIdSindicato(idSindicato).orElse(empregado.getId());
        if (!dono.equals(empregado.getId())) {
            throw new IdSindicatoDuplicadoException();
        }
        empregado.sindicalizar(idSindicato, taxaSindical);
        dados.atualizarIdSindicato(empregado.getId(), idSindicato);
    }

    /** Parte de alteraEmpregado: chamado dentro da transacao do comando. */
    void dessindicalizar(Empregado empregado) throws WePayUException {
        empregado.dessindicalizar();
        sessao.dados().atualizarIdSindicato(empregado.getId(), null);
    }
}
