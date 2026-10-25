package br.ufal.ic.p2.wepayu.modelos;

import br.ufal.ic.p2.wepayu.excecoes.EmpregadoNaoSindicalizadoException;
import br.ufal.ic.p2.wepayu.modelos.agenda.AgendaPagamento;
import br.ufal.ic.p2.wepayu.util.Datas;
import br.ufal.ic.p2.wepayu.util.Dinheiro;
import br.ufal.ic.p2.wepayu.util.Exigir;
import br.ufal.ic.p2.wepayu.util.Periodo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Um empregado da folha de pagamento. Horista, assalariado e comissionado
 * sao subclasses concretas; o que so vale para um tipo (cartao de ponto,
 * venda, comissao) fica so na subclasse correspondente.
 *
 * Quando o empregado e pago, e qual periodo cada pagamento cobre, vem da
 * agenda de pagamento dele; a subclasse so diz quanto ganhou no periodo.
 */
public abstract class Empregado implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private String nome;
    private String endereco;
    private BigDecimal salario;
    private MetodoPagamento metodoPagamento = new EmMaos();
    private MembroSindicato sindicato;
    private AgendaPagamento agenda;

    // null so para o horista que ainda nao lancou cartao: o enunciado diz que
    // ele e contratado no primeiro dia em que lancar um.
    private LocalDate dataContratacao;

    // ultimo dia cuja taxa sindical ja foi cobrada (o dia do ultimo pagamento,
    // ou a vespera da contratacao, antes do primeiro).
    private LocalDate ultimoDiaPago;

    // ultimo pagamento confirmado e pra que data -- rodar a folha de novo na
    // mesma data deve dar o mesmo resultado (ver calcularPagamento).
    private LocalDate ultimaDataProcessada;
    private Pagamento ultimoPagamentoCalculado;

    protected Empregado(String id, String nome, String endereco, BigDecimal salario,
                        AgendaPagamento agenda, LocalDate dataContratacao) {
        this.id = Exigir.preenchido(id, "Identificacao do empregado");
        setNome(nome);
        setEndereco(endereco);
        setSalario(salario);
        setAgenda(agenda);
        if (dataContratacao != null) {
            definirContratacao(dataContratacao);
        }
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

    public AgendaPagamento getAgenda() {
        return agenda;
    }

    public final void setAgenda(AgendaPagamento agenda) {
        this.agenda = Exigir.naoNulo(agenda, "Agenda de pagamento");
    }

    public abstract TipoEmpregado getTipo();

    /** So o horista define a contratacao depois de criado (no primeiro cartao). */
    protected final void definirContratacao(LocalDate data) {
        dataContratacao = data;
        ultimoDiaPago = data.minusDays(1);
    }

    protected final boolean jaContratado() {
        return dataContratacao != null;
    }

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

    /** Filia ao sindicato; se ja for filiado, so troca os dados e mantem taxas e divida. */
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

    // ---- folha de pagamento ----

    public boolean estaNaDataDePagamento(LocalDate data) {
        return agenda.ehDiaDePagamento(data);
    }

    /**
     * Calcula o pagamento desta data sem mudar nada no empregado: totalFolha
     * so quer saber quanto seria, e o rodaFolha confirma depois. Repetir a
     * data de um pagamento ja confirmado devolve o mesmo resultado.
     */
    public final Pagamento calcularPagamento(LocalDate data) {
        if (data.equals(ultimaDataProcessada)) {
            return ultimoPagamentoCalculado;
        }
        SalarioBruto bruto = contratadoAte(data) ? calcularBruto(periodoPagoEm(data)) : SalarioBruto.nenhum();
        return descontar(bruto, data);
    }

    /** Quanto o empregado ganhou no periodo (as regras de cada tipo). */
    protected abstract SalarioBruto calcularBruto(Periodo periodo);

    /** O salario fixo que cabe num pagamento, conforme a agenda (mensal inteiro, ou salario*12*N/52). */
    protected BigDecimal parcelaDoSalarioFixo() {
        return Dinheiro.truncar(agenda.parcelaDoSalarioMensal(salario));
    }

    /** So o rodaFolha de verdade chama isto. */
    public void confirmarPagamento(Pagamento pagamento, LocalDate data) {
        if (sindicato != null) {
            sindicato.registrarDivida(pagamento.dividaSindicalRestante());
        }
        if (contratadoAte(data)) {
            ultimoDiaPago = data;
        }
        ultimaDataProcessada = data;
        ultimoPagamentoCalculado = pagamento;
    }

    private boolean contratadoAte(LocalDate data) {
        return dataContratacao != null && !dataContratacao.isAfter(data);
    }

    /** Do dia seguinte ao pagamento anterior (pela agenda) ate a data, inclusive; nunca antes da contratacao. */
    private Periodo periodoPagoEm(LocalDate data) {
        LocalDate inicio = agenda.inicioDoPeriodo(data);
        if (inicio.isBefore(dataContratacao)) {
            inicio = dataContratacao;
        }
        return new Periodo(inicio, data.plusDays(1));
    }

    /** O contracheque nunca fica negativo: o que nao couber vira divida para o proximo (Obs. 1 da US7). */
    private Pagamento descontar(SalarioBruto bruto, LocalDate data) {
        BigDecimal devido = descontosDevidosAte(data);
        BigDecimal aplicado = devido.min(bruto.valor());
        return new Pagamento(nome, getTipo(), bruto, aplicado, bruto.valor().subtract(aplicado),
                metodoPagamento.getDescricao(endereco), devido.subtract(aplicado));
    }

    /** Divida anterior + taxa sindical diaria desde o ultimo pagamento + taxas de servico do periodo. */
    private BigDecimal descontosDevidosAte(LocalDate data) {
        if (sindicato == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal devido = sindicato.getDivida();
        if (!contratadoAte(data) || !data.isAfter(ultimoDiaPago)) {
            return devido;
        }
        BigDecimal dias = BigDecimal.valueOf(Datas.diasEntre(ultimoDiaPago, data));
        Periodo periodo = new Periodo(ultimoDiaPago.plusDays(1), data.plusDays(1));
        return devido.add(sindicato.getTaxaSindical().multiply(dias))
                .add(sindicato.getTaxasServicoNoPeriodo(periodo));
    }
}
