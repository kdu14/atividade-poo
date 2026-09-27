package br.ufal.ic.p2.wepayu.modelos;

import br.ufal.ic.p2.wepayu.util.Datas;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Um empregado da folha de pagamento. Horista, assalariado e comissionado
 * sao subclasses concretas; operacoes que so fazem sentido pra algum tipo
 * (bater cartao, lancar venda, ter comissao) tem aqui uma implementacao
 * padrao que lanca erro, sobrescrita so onde se aplica -- assim ninguem
 * precisa checar o tipo do empregado antes de chamar o metodo.
 */
public abstract class Empregado implements Serializable {

    private final String id;
    private String nome;
    private String endereco;
    private BigDecimal salario;
    private MetodoPagamento metodoPagamento = new EmMaos();

    private boolean sindicalizado = false;
    private String idSindicato;
    private BigDecimal taxaSindical;
    private final List<TaxaServico> taxasServico = new ArrayList<>();

    // dia anterior ao inicio do proximo periodo a pagar: o periodo de um
    // pagamento e sempre (dataBase, dataFolha]. Null pro horista que ainda
    // nao lancou cartao nenhum (a contratacao so e conhecida no primeiro).
    private LocalDate dataBase;

    // ultimo pagamento calculado e pra que data -- necessario pra rodar a
    // folha duas vezes na mesma data dar o mesmo resultado (ver calcularPagamento).
    private LocalDate ultimaDataProcessada;
    private Pagamento ultimoPagamentoCalculado;

    protected Empregado(String id, String nome, String endereco, BigDecimal salario) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.salario = salario;
    }

    // ---- identificacao e dados basicos ----

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public BigDecimal getSalario() {
        return salario;
    }

    public void setSalario(BigDecimal salario) {
        this.salario = salario;
    }

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(MetodoPagamento metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    /** "horista", "assalariado" ou "comissionado" -- o mesmo texto que getAtributoEmpregado devolve. */
    public abstract String getTipo();

    // ---- sindicato ----

    public boolean isSindicalizado() {
        return sindicalizado;
    }

    public String getIdSindicato() {
        return idSindicato;
    }

    public BigDecimal getTaxaSindical() {
        return taxaSindical;
    }

    public void sindicalizar(String idSindicato, BigDecimal taxaSindicalDiaria) {
        this.sindicalizado = true;
        this.idSindicato = idSindicato;
        this.taxaSindical = taxaSindicalDiaria;
    }

    public void dessindicalizar() {
        this.sindicalizado = false;
        this.idSindicato = null;
        this.taxaSindical = null;
        this.taxasServico.clear();
    }

    public void lancarTaxaServico(LocalDate data, BigDecimal valor) {
        taxasServico.add(new TaxaServico(data, valor));
    }

    public BigDecimal getTaxasServicoNoPeriodo(LocalDate inicioInclusivo, LocalDate fimExclusivo) {
        BigDecimal total = BigDecimal.ZERO;
        for (TaxaServico t : taxasServico) {
            if (Datas.dentroDoIntervalo(t.getData(), inicioInclusivo, fimExclusivo)) {
                total = total.add(t.getValor());
            }
        }
        return total;
    }

    // ---- operacoes que so fazem sentido para alguns tipos ----

    public void lancarCartao(LocalDate data, BigDecimal horas) {
        throw new UnsupportedOperationException("Empregado nao eh horista.");
    }

    public BigDecimal getHorasNormaisNoPeriodo(LocalDate inicioInclusivo, LocalDate fimExclusivo) {
        throw new UnsupportedOperationException("Empregado nao eh horista.");
    }

    public BigDecimal getHorasExtrasNoPeriodo(LocalDate inicioInclusivo, LocalDate fimExclusivo) {
        throw new UnsupportedOperationException("Empregado nao eh horista.");
    }

    public void lancarVenda(LocalDate data, BigDecimal valor) {
        throw new UnsupportedOperationException("Empregado nao eh comissionado.");
    }

    public BigDecimal getVendasNoPeriodo(LocalDate inicioInclusivo, LocalDate fimExclusivo) {
        throw new UnsupportedOperationException("Empregado nao eh comissionado.");
    }

    public BigDecimal getComissao() {
        throw new UnsupportedOperationException("Empregado nao eh comissionado.");
    }

    public void setComissao(BigDecimal comissao) {
        throw new UnsupportedOperationException("Empregado nao eh comissionado.");
    }

    // ---- agenda e calculo de pagamento ----

    protected LocalDate getDataBase() {
        return dataBase;
    }

    protected void setDataBase(LocalDate dataBase) {
        this.dataBase = dataBase;
    }

    /** Esta data e um dia em que ESTE empregado deve ser considerado na folha? */
    public abstract boolean estaNaDataDePagamento(LocalDate data);

    /**
     * Calcula o pagamento pra esta data, sem mudar nada no empregado (usado
     * tanto por totalFolha, que so quer saber "quanto seria", quanto pelo
     * rodaFolha de verdade, que so muda algo depois via confirmarPagamento).
     * Repetir a mesma data devolve o resultado ja calculado antes, em vez de
     * recalcular sobre um periodo vazio (a dataBase ja teria avancado).
     */
    public final Pagamento calcularPagamento(LocalDate data) {
        if (data.equals(ultimaDataProcessada)) {
            return ultimoPagamentoCalculado;
        }
        return calcularPagamentoNovo(data);
    }

    protected abstract Pagamento calcularPagamentoNovo(LocalDate data);

    /** So o rodaFolha de verdade chama isto -- avanca a dataBase, exceto quando o pagamento ficou zerado (ver calcularDescontos). */
    public void confirmarPagamento(Pagamento pagamento, LocalDate data) {
        if (pagamento.avancaPeriodo()) {
            dataBase = data;
        }
        ultimaDataProcessada = data;
        ultimoPagamentoCalculado = pagamento;
    }

    /**
     * Desconto sindical devido no periodo (dataBase, data], sem deixar o
     * contracheque negativo (Obs. 1 da US7 -- so vale pra horista, mas
     * aplicar geral nao muda nada nos outros tipos). Quando corta, devolve
     * avancaPeriodo=false: a dataBase fica parada, e o que faltou pagar
     * se acumula sozinho, porque o proximo calculo conta os dias desde ali.
     */
    protected ResultadoDesconto calcularDescontos(LocalDate data, BigDecimal salarioBruto) {
        BigDecimal descontosDevidos = calcularDescontosDevidos(data);
        if (descontosDevidos.compareTo(salarioBruto) > 0) {
            return new ResultadoDesconto(salarioBruto, BigDecimal.ZERO, false);
        }
        return new ResultadoDesconto(descontosDevidos, salarioBruto.subtract(descontosDevidos), true);
    }

    private BigDecimal calcularDescontosDevidos(LocalDate data) {
        if (!sindicalizado) {
            return BigDecimal.ZERO;
        }
        LocalDate inicioPeriodo = dataBase.plusDays(1);
        long dias = Datas.diasEntre(dataBase, data);
        BigDecimal totalSindical = taxaSindical.multiply(BigDecimal.valueOf(dias));
        BigDecimal totalServicos = getTaxasServicoNoPeriodo(inicioPeriodo, data);
        return totalSindical.add(totalServicos);
    }

    protected record ResultadoDesconto(BigDecimal aplicados, BigDecimal liquido, boolean avancaPeriodo) {
    }
}
