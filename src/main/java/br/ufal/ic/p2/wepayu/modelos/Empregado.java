package br.ufal.ic.p2.wepayu.modelos;

import br.ufal.ic.p2.wepayu.excecoes.EmpregadoNaoSindicalizadoException;
import br.ufal.ic.p2.wepayu.util.Datas;
import br.ufal.ic.p2.wepayu.util.Exigir;
import br.ufal.ic.p2.wepayu.util.Periodo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Um empregado da folha de pagamento. Horista, assalariado e comissionado
 * sao subclasses concretas; o que so vale para um tipo (cartao de ponto,
 * venda, comissao) fica so na subclasse correspondente.
 */
public abstract class Empregado implements Serializable {

    private static final long serialVersionUID = 1L;

    // O enunciado (US7) pede pra considerar todo assalariado/comissionado
    // contratado em 1/1/2005, ja que criarEmpregado nao tem esse campo.
    protected static final LocalDate DATA_CONTRATACAO_PADRAO = LocalDate.of(2005, 1, 1);

    private final String id;
    private String nome;
    private String endereco;
    private BigDecimal salario;
    private MetodoPagamento metodoPagamento = new EmMaos();
    private MembroSindicato sindicato;

    // dia anterior ao inicio do proximo periodo a pagar. Null pro horista que
    // ainda nao lancou cartao nenhum (a contratacao so e conhecida no primeiro).
    private LocalDate dataBase;

    // ultimo pagamento calculado e pra que data -- necessario pra rodar a
    // folha duas vezes na mesma data dar o mesmo resultado (ver calcularPagamento).
    private LocalDate ultimaDataProcessada;
    private Pagamento ultimoPagamentoCalculado;

    protected Empregado(String id, String nome, String endereco, BigDecimal salario, LocalDate dataBaseInicial) {
        this.id = Exigir.preenchido(id, "Identificacao do empregado");
        setNome(nome);
        setEndereco(endereco);
        setSalario(salario);
        this.dataBase = dataBaseInicial;
    }

    // ---- identificacao e dados basicos ----

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public final void setNome(String nome) {
        this.nome = Exigir.preenchido(nome, "Nome");
    }

    public String getEndereco() {
        return endereco;
    }

    public final void setEndereco(String endereco) {
        this.endereco = Exigir.preenchido(endereco, "Endereco");
    }

    public BigDecimal getSalario() {
        return salario;
    }

    public final void setSalario(BigDecimal salario) {
        this.salario = Exigir.naoNegativo(salario, "Salario");
    }

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(MetodoPagamento metodoPagamento) {
        this.metodoPagamento = Exigir.naoNulo(metodoPagamento, "Metodo de pagamento");
    }

    public abstract TipoEmpregado getTipo();

    // ---- sindicato ----

    public boolean isSindicalizado() {
        return sindicato != null;
    }

    public MembroSindicato getSindicato() throws EmpregadoNaoSindicalizadoException {
        if (sindicato == null) {
            throw new EmpregadoNaoSindicalizadoException();
        }
        return sindicato;
    }

    /** Filia ao sindicato; se ja for filiado, so troca os dados e mantem as taxas lancadas. */
    public void sindicalizar(String idSindicato, BigDecimal taxaSindical) {
        sindicato = (sindicato == null)
                ? new MembroSindicato(idSindicato, taxaSindical)
                : sindicato.comNovosDados(idSindicato, taxaSindical);
    }

    public void dessindicalizar() {
        sindicato = null;
    }

    /** Usado na troca de tipo: o que nao depende do tipo passa para o novo objeto. */
    public void transferirDadosComunsPara(Empregado novo) {
        novo.metodoPagamento = metodoPagamento;
        novo.sindicato = sindicato;
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
     * Desconto sindical devido no periodo, sem deixar o contracheque negativo
     * (Obs. 1 da US7). Quando corta, devolve avancaPeriodo=false: a dataBase
     * fica parada, e o que faltou pagar se acumula no proximo calculo.
     */
    protected ResultadoDesconto calcularDescontos(LocalDate data, BigDecimal salarioBruto) {
        BigDecimal descontosDevidos = calcularDescontosDevidos(data);
        if (descontosDevidos.compareTo(salarioBruto) > 0) {
            return new ResultadoDesconto(salarioBruto, BigDecimal.ZERO, false);
        }
        return new ResultadoDesconto(descontosDevidos, salarioBruto.subtract(descontosDevidos), true);
    }

    private BigDecimal calcularDescontosDevidos(LocalDate data) {
        if (sindicato == null) {
            return BigDecimal.ZERO;
        }
        long dias = Datas.diasEntre(dataBase, data);
        BigDecimal totalSindical = sindicato.getTaxaSindical().multiply(BigDecimal.valueOf(dias));
        BigDecimal totalServicos = sindicato.getTaxasServicoNoPeriodo(new Periodo(dataBase.plusDays(1), data));
        return totalSindical.add(totalServicos);
    }

    protected Pagamento novoPagamento(ComposicaoDoBruto composicao, BigDecimal bruto, LocalDate data) {
        ResultadoDesconto desconto = calcularDescontos(data, bruto);
        return new Pagamento(nome, getTipo(), composicao, bruto, desconto.aplicados(), desconto.liquido(),
                metodoPagamento.getDescricao(endereco), desconto.avancaPeriodo());
    }

    protected record ResultadoDesconto(BigDecimal aplicados, BigDecimal liquido, boolean avancaPeriodo) {
    }
}
