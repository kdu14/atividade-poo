package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.excecoes.EmpregadoException;
import br.ufal.ic.p2.wepayu.modelos.*;
import br.ufal.ic.p2.wepayu.util.Datas;
import br.ufal.ic.p2.wepayu.util.Dinheiro;
import br.ufal.ic.p2.wepayu.util.Formatador;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * O nucleo de negocio do sistema de folha de pagamento. A Facade so traduz
 * a linguagem de script do EasyAccept pra chamadas nesta classe -- toda
 * regra de negocio (validacao, calculo de salario, undo/redo, persistencia)
 * mora aqui.
 */
public class Sistema {

    private static final String ARQUIVO_PERSISTENCIA = "wepayu.dat";

    private Map<String, Empregado> empregados = new LinkedHashMap<>();
    private Map<String, String> indiceSindicato = new LinkedHashMap<>();
    private int proximoId = 1;
    private boolean encerrado = false;

    // Pilhas de undo/redo: cada elemento e uma "foto" (serializada) do estado
    // ANTES de um comando mutavel rodar. Nao sao persistidas em disco -- cada
    // instancia de Facade comeca com as pilhas vazias, o que e exatamente o
    // que a User Story 8 espera (undo nao atravessa reinicios do processo).
    private final Deque<byte[]> pilhaUndo = new ArrayDeque<>();
    private final Deque<byte[]> pilhaRedo = new ArrayDeque<>();

    public Sistema() {
        carregarDoDisco();
    }

    // ==================== ciclo de vida ====================

    public void zerarSistema() throws EmpregadoException {
        verificarNaoEncerrado();
        executarComMemento(() -> {
            empregados = new LinkedHashMap<>();
            indiceSindicato = new LinkedHashMap<>();
            proximoId = 1;
            return null;
        });
    }

    public void encerrarSistema() throws EmpregadoException {
        verificarNaoEncerrado();
        salvarNoDisco();
        encerrado = true;
    }

    public void undo() throws EmpregadoException {
        verificarNaoEncerrado();
        if (pilhaUndo.isEmpty()) {
            throw new EmpregadoException("Nao ha comando a desfazer.");
        }
        byte[] fotoAtual = tirarFoto();
        restaurarFoto(pilhaUndo.pop());
        pilhaRedo.push(fotoAtual);
    }

    public void redo() throws EmpregadoException {
        verificarNaoEncerrado();
        if (pilhaRedo.isEmpty()) {
            throw new EmpregadoException("Nao ha comando a refazer.");
        }
        byte[] fotoAtual = tirarFoto();
        restaurarFoto(pilhaRedo.pop());
        pilhaUndo.push(fotoAtual);
    }

    // ==================== empregados ====================

    public String criarEmpregado(String nome, String endereco, String tipo, String salarioTexto) throws EmpregadoException {
        verificarNaoEncerrado();
        return executarComMemento(() -> criarEmpregadoInterno(nome, endereco, tipo, salarioTexto, null));
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salarioTexto, String comissaoTexto) throws EmpregadoException {
        verificarNaoEncerrado();
        return executarComMemento(() -> criarEmpregadoInterno(nome, endereco, tipo, salarioTexto, comissaoTexto));
    }

    private String criarEmpregadoInterno(String nome, String endereco, String tipo, String salarioTexto, String comissaoTexto) throws EmpregadoException {
        validarNaoVazio(nome, "Nome nao pode ser nulo.");
        validarNaoVazio(endereco, "Endereco nao pode ser nulo.");
        validarTipoConhecido(tipo);

        boolean tipoExigeComissao = "comissionado".equals(tipo);
        if (tipoExigeComissao != (comissaoTexto != null)) {
            throw new EmpregadoException("Tipo nao aplicavel.");
        }

        BigDecimal salario = validarSalario(salarioTexto);
        String id = String.valueOf(proximoId++);

        Empregado empregado;
        if ("horista".equals(tipo)) {
            empregado = new EmpregadoHorista(id, nome, endereco, salario);
        } else if ("assalariado".equals(tipo)) {
            empregado = new EmpregadoAssalariado(id, nome, endereco, salario);
        } else {
            BigDecimal comissao = validarComissao(comissaoTexto);
            empregado = new EmpregadoComissionado(id, nome, endereco, salario, comissao);
        }

        empregados.put(id, empregado);
        return id;
    }

    public void removerEmpregado(String emp) throws EmpregadoException {
        verificarNaoEncerrado();
        validarIdentificacaoNaoVazia(emp);
        buscarEmpregado(emp);
        executarComMemento(() -> {
            Empregado removido = empregados.remove(emp);
            if (removido.isSindicalizado()) {
                indiceSindicato.remove(removido.getIdSindicato());
            }
            return null;
        });
    }

    public int getNumeroDeEmpregados() throws EmpregadoException {
        verificarNaoEncerrado();
        return empregados.size();
    }

    public String getEmpregadoPorNome(String nome, String indiceTexto) throws EmpregadoException {
        verificarNaoEncerrado();
        int indice = Integer.parseInt(indiceTexto.trim());
        int encontrados = 0;
        for (Empregado e : empregados.values()) {
            if (e.getNome().contains(nome)) {
                encontrados++;
                if (encontrados == indice) {
                    return e.getId();
                }
            }
        }
        throw new EmpregadoException("Nao ha empregado com esse nome.");
    }

    public String getAtributoEmpregado(String emp, String atributo) throws EmpregadoException {
        verificarNaoEncerrado();
        Empregado e = buscarEmpregado(emp);
        return switch (atributo) {
            case "nome" -> e.getNome();
            case "endereco" -> e.getEndereco();
            case "tipo" -> e.getTipo();
            case "salario" -> Dinheiro.formatar(e.getSalario());
            case "comissao" -> Dinheiro.formatar(chamarPolimorfico(e::getComissao));
            case "sindicalizado" -> e.isSindicalizado() ? "true" : "false";
            case "idSindicato" -> {
                exigirSindicalizado(e);
                yield e.getIdSindicato();
            }
            case "taxaSindical" -> {
                exigirSindicalizado(e);
                yield Dinheiro.formatar(e.getTaxaSindical());
            }
            case "metodoPagamento" -> e.getMetodoPagamento().getNome();
            case "banco" -> {
                exigirRecebeEmBanco(e);
                yield e.getMetodoPagamento().getBanco();
            }
            case "agencia" -> {
                exigirRecebeEmBanco(e);
                yield e.getMetodoPagamento().getAgencia();
            }
            case "contaCorrente" -> {
                exigirRecebeEmBanco(e);
                yield e.getMetodoPagamento().getContaCorrente();
            }
            default -> throw new EmpregadoException("Atributo nao existe.");
        };
    }

    // ==================== alteracao de empregado ====================

    public void alteraEmpregado(String emp, String atributo, String valor) throws EmpregadoException {
        verificarNaoEncerrado();
        Empregado e = buscarEmpregado(emp);
        executarComMemento(() -> {
            alterarAtributoSimples(e, atributo, valor);
            return null;
        });
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String extra) throws EmpregadoException {
        verificarNaoEncerrado();
        buscarEmpregado(emp);
        if (!"tipo".equals(atributo)) {
            throw new EmpregadoException("Atributo nao existe.");
        }
        executarComMemento(() -> {
            mudarTipo(emp, valor, extra);
            return null;
        });
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String idSindicato, String taxaSindicalTexto) throws EmpregadoException {
        verificarNaoEncerrado();
        Empregado e = buscarEmpregado(emp);
        if (!"sindicalizado".equals(atributo)) {
            throw new EmpregadoException("Atributo nao existe.");
        }
        executarComMemento(() -> {
            sindicalizarEmpregado(emp, e, idSindicato, taxaSindicalTexto);
            return null;
        });
    }

    public void alteraEmpregado(String emp, String atributo, String valor1, String banco, String agencia, String contaCorrente) throws EmpregadoException {
        verificarNaoEncerrado();
        Empregado e = buscarEmpregado(emp);
        if (!"metodoPagamento".equals(atributo)) {
            throw new EmpregadoException("Atributo nao existe.");
        }
        executarComMemento(() -> {
            validarNaoVazio(banco, "Banco nao pode ser nulo.");
            validarNaoVazio(agencia, "Agencia nao pode ser nulo.");
            validarNaoVazio(contaCorrente, "Conta corrente nao pode ser nulo.");
            e.setMetodoPagamento(new EmBanco(banco, agencia, contaCorrente));
            return null;
        });
    }

    private void alterarAtributoSimples(Empregado e, String atributo, String valor) throws EmpregadoException {
        switch (atributo) {
            case "nome" -> {
                validarNaoVazio(valor, "Nome nao pode ser nulo.");
                e.setNome(valor);
            }
            case "endereco" -> {
                validarNaoVazio(valor, "Endereco nao pode ser nulo.");
                e.setEndereco(valor);
            }
            case "salario" -> e.setSalario(validarSalario(valor));
            case "comissao" -> {
                exigirComissionado(e);
                e.setComissao(validarComissao(valor));
            }
            case "tipo" -> mudarTipo(e.getId(), valor, null);
            case "sindicalizado" -> {
                if ("false".equals(valor)) {
                    removerDoIndiceSindicato(e);
                    e.dessindicalizar();
                } else if (!"true".equals(valor)) {
                    throw new EmpregadoException("Valor deve ser true ou false.");
                } else {
                    throw new EmpregadoException("Identificacao do sindicato nao pode ser nula.");
                }
            }
            case "metodoPagamento" -> {
                if ("correios".equals(valor)) {
                    e.setMetodoPagamento(new Correios());
                } else if ("emMaos".equals(valor)) {
                    e.setMetodoPagamento(new EmMaos());
                } else {
                    throw new EmpregadoException("Metodo de pagamento invalido.");
                }
            }
            default -> throw new EmpregadoException("Atributo nao existe.");
        }
    }

    // troca o tipo preservando nome/endereco/metodo/sindicato; "extra" e
    // opcional pra horista/assalariado (mantem o salario de antes se nao vier),
    // mas obrigatorio pra comissionado, que nunca teria uma comissao previa.
    private void mudarTipo(String id, String tipo, String extra) throws EmpregadoException {
        validarTipoConhecido(tipo);
        Empregado antigo = empregados.get(id);

        Empregado novo;
        if ("comissionado".equals(tipo)) {
            if (extra == null) {
                throw new EmpregadoException("Tipo nao aplicavel.");
            }
            BigDecimal comissao = validarComissao(extra);
            novo = new EmpregadoComissionado(id, antigo.getNome(), antigo.getEndereco(), antigo.getSalario(), comissao);
        } else {
            BigDecimal salario = (extra == null) ? antigo.getSalario() : validarSalario(extra);
            novo = "horista".equals(tipo)
                    ? new EmpregadoHorista(id, antigo.getNome(), antigo.getEndereco(), salario)
                    : new EmpregadoAssalariado(id, antigo.getNome(), antigo.getEndereco(), salario);
        }

        novo.setMetodoPagamento(antigo.getMetodoPagamento());
        if (antigo.isSindicalizado()) {
            novo.sindicalizar(antigo.getIdSindicato(), antigo.getTaxaSindical());
        }
        empregados.put(id, novo);
    }

    private void sindicalizarEmpregado(String id, Empregado e, String idSindicato, String taxaSindicalTexto) throws EmpregadoException {
        validarNaoVazio(idSindicato, "Identificacao do sindicato nao pode ser nula.");
        BigDecimal taxaSindical = validarValorNaoNegativo(taxaSindicalTexto,
                "Taxa sindical nao pode ser nula.",
                "Taxa sindical deve ser numerica.",
                "Taxa sindical deve ser nao-negativa.");

        String donoAtual = indiceSindicato.get(idSindicato);
        if (donoAtual != null && !donoAtual.equals(id)) {
            throw new EmpregadoException("Ha outro empregado com esta identificacao de sindicato");
        }

        removerDoIndiceSindicato(e);
        e.sindicalizar(idSindicato, taxaSindical);
        indiceSindicato.put(idSindicato, id);
    }

    private void removerDoIndiceSindicato(Empregado e) {
        if (e.isSindicalizado()) {
            indiceSindicato.remove(e.getIdSindicato());
        }
    }

    // ==================== lancamentos ====================

    public void lancaCartao(String emp, String dataTexto, String horasTexto) throws EmpregadoException {
        verificarNaoEncerrado();
        Empregado e = buscarEmpregado(emp);
        LocalDate data = validarData(dataTexto, "Data invalida.");
        BigDecimal horas = validarValorPositivo(horasTexto, "Horas devem ser positivas.");
        executarComMemento(() -> {
            try {
                e.lancarCartao(data, horas);
            } catch (UnsupportedOperationException ex) {
                throw new EmpregadoException(ex.getMessage());
            }
            return null;
        });
    }

    public void lancaVenda(String emp, String dataTexto, String valorTexto) throws EmpregadoException {
        verificarNaoEncerrado();
        Empregado e = buscarEmpregado(emp);
        LocalDate data = validarData(dataTexto, "Data invalida.");
        BigDecimal valor = validarValorPositivo(valorTexto, "Valor deve ser positivo.");
        executarComMemento(() -> {
            try {
                e.lancarVenda(data, valor);
            } catch (UnsupportedOperationException ex) {
                throw new EmpregadoException(ex.getMessage());
            }
            return null;
        });
    }

    public void lancaTaxaServico(String membro, String dataTexto, String valorTexto) throws EmpregadoException {
        verificarNaoEncerrado();
        validarNaoVazio(membro, "Identificacao do membro nao pode ser nula.");
        String idEmpregado = indiceSindicato.get(membro);
        if (idEmpregado == null) {
            throw new EmpregadoException("Membro nao existe.");
        }
        LocalDate data = validarData(dataTexto, "Data invalida.");
        BigDecimal valor = validarValorPositivo(valorTexto, "Valor deve ser positivo.");
        executarComMemento(() -> {
            empregados.get(idEmpregado).lancarTaxaServico(data, valor);
            return null;
        });
    }

    // ==================== consultas por periodo ====================

    public String getHorasNormaisTrabalhadas(String emp, String dataInicialTexto, String dataFinalTexto) throws EmpregadoException {
        verificarNaoEncerrado();
        Empregado e = buscarEmpregado(emp);
        LocalDate[] intervalo = validarIntervalo(dataInicialTexto, dataFinalTexto);
        BigDecimal horas = chamarPolimorfico(() -> e.getHorasNormaisNoPeriodo(intervalo[0], intervalo[1]));
        return Formatador.formatarHoras(horas);
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicialTexto, String dataFinalTexto) throws EmpregadoException {
        verificarNaoEncerrado();
        Empregado e = buscarEmpregado(emp);
        LocalDate[] intervalo = validarIntervalo(dataInicialTexto, dataFinalTexto);
        BigDecimal horas = chamarPolimorfico(() -> e.getHorasExtrasNoPeriodo(intervalo[0], intervalo[1]));
        return Formatador.formatarHoras(horas);
    }

    public String getVendasRealizadas(String emp, String dataInicialTexto, String dataFinalTexto) throws EmpregadoException {
        verificarNaoEncerrado();
        Empregado e = buscarEmpregado(emp);
        LocalDate[] intervalo = validarIntervalo(dataInicialTexto, dataFinalTexto);
        BigDecimal vendas = chamarPolimorfico(() -> e.getVendasNoPeriodo(intervalo[0], intervalo[1]));
        return Dinheiro.formatar(vendas);
    }

    public String getTaxasServico(String emp, String dataInicialTexto, String dataFinalTexto) throws EmpregadoException {
        verificarNaoEncerrado();
        Empregado e = buscarEmpregado(emp);
        exigirSindicalizado(e);
        LocalDate[] intervalo = validarIntervalo(dataInicialTexto, dataFinalTexto);
        BigDecimal taxas = e.getTaxasServicoNoPeriodo(intervalo[0], intervalo[1]);
        return Dinheiro.formatar(taxas);
    }

    // ==================== folha de pagamento ====================

    public String totalFolha(String dataTexto) throws EmpregadoException {
        verificarNaoEncerrado();
        LocalDate data = validarData(dataTexto, "Data invalida.");
        BigDecimal total = BigDecimal.ZERO;
        for (Empregado e : empregados.values()) {
            if (e.estaNaDataDePagamento(data)) {
                total = total.add(e.calcularPagamento(data).salarioBruto());
            }
        }
        return Dinheiro.formatar(total);
    }

    public void rodaFolha(String dataTexto, String arquivoSaida) throws EmpregadoException {
        verificarNaoEncerrado();
        LocalDate data = validarData(dataTexto, "Data invalida.");
        executarComMemento(() -> {
            List<Pagamento> horistas = new ArrayList<>();
            List<Pagamento> assalariados = new ArrayList<>();
            List<Pagamento> comissionados = new ArrayList<>();

            for (Empregado e : empregados.values()) {
                if (!e.estaNaDataDePagamento(data)) {
                    continue;
                }
                Pagamento pagamento = e.calcularPagamento(data);
                e.confirmarPagamento(pagamento, data);
                switch (e.getTipo()) {
                    case "horista" -> horistas.add(pagamento);
                    case "assalariado" -> assalariados.add(pagamento);
                    case "comissionado" -> comissionados.add(pagamento);
                    default -> throw new IllegalStateException("Tipo de empregado desconhecido: " + e.getTipo());
                }
            }

            String relatorio = Formatador.montarRelatorio(data, horistas, assalariados, comissionados);
            escreverArquivo(arquivoSaida, relatorio);
            return null;
        });
    }

    private void escreverArquivo(String caminho, String conteudo) throws EmpregadoException {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(caminho), StandardCharsets.UTF_8)) {
            w.write(conteudo);
        } catch (IOException ex) {
            throw new EmpregadoException("Nao foi possivel escrever o arquivo de saida: " + ex.getMessage());
        }
    }

    // ==================== validacoes compartilhadas ====================

    private void verificarNaoEncerrado() throws EmpregadoException {
        if (encerrado) {
            throw new EmpregadoException("Nao pode dar comandos depois de encerrarSistema.");
        }
    }

    private void validarIdentificacaoNaoVazia(String emp) throws EmpregadoException {
        if (emp == null || emp.isEmpty()) {
            throw new EmpregadoException("Identificacao do empregado nao pode ser nula.");
        }
    }

    private Empregado buscarEmpregado(String emp) throws EmpregadoException {
        validarIdentificacaoNaoVazia(emp);
        Empregado e = empregados.get(emp);
        if (e == null) {
            throw new EmpregadoException("Empregado nao existe.");
        }
        return e;
    }

    private void exigirSindicalizado(Empregado e) throws EmpregadoException {
        if (!e.isSindicalizado()) {
            throw new EmpregadoException("Empregado nao eh sindicalizado.");
        }
    }

    private void exigirRecebeEmBanco(Empregado e) throws EmpregadoException {
        if (!e.getMetodoPagamento().recebeEmBanco()) {
            throw new EmpregadoException("Empregado nao recebe em banco.");
        }
    }

    private void exigirComissionado(Empregado e) throws EmpregadoException {
        if (!"comissionado".equals(e.getTipo())) {
            throw new EmpregadoException("Empregado nao eh comissionado.");
        }
    }

    private void validarNaoVazio(String valor, String mensagem) throws EmpregadoException {
        if (valor == null || valor.isEmpty()) {
            throw new EmpregadoException(mensagem);
        }
    }

    private void validarTipoConhecido(String tipo) throws EmpregadoException {
        if (!"horista".equals(tipo) && !"assalariado".equals(tipo) && !"comissionado".equals(tipo)) {
            throw new EmpregadoException("Tipo invalido.");
        }
    }

    private BigDecimal validarSalario(String texto) throws EmpregadoException {
        return validarValorNaoNegativo(texto,
                "Salario nao pode ser nulo.",
                "Salario deve ser numerico.",
                "Salario deve ser nao-negativo.");
    }

    private BigDecimal validarComissao(String texto) throws EmpregadoException {
        return validarValorNaoNegativo(texto,
                "Comissao nao pode ser nula.",
                "Comissao deve ser numerica.",
                "Comissao deve ser nao-negativa.");
    }

    private BigDecimal validarValorNaoNegativo(String texto, String mensagemNulo, String mensagemNumerico, String mensagemNegativo)
            throws EmpregadoException {
        if (texto == null || texto.isEmpty()) {
            throw new EmpregadoException(mensagemNulo);
        }
        if (!Dinheiro.ehNumerico(texto)) {
            throw new EmpregadoException(mensagemNumerico);
        }
        BigDecimal valor = Dinheiro.parse(texto);
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new EmpregadoException(mensagemNegativo);
        }
        return valor;
    }

    private BigDecimal validarValorPositivo(String texto, String mensagemErro) throws EmpregadoException {
        BigDecimal valor;
        if (texto == null || !Dinheiro.ehNumerico(texto)) {
            throw new EmpregadoException(mensagemErro);
        }
        valor = Dinheiro.parse(texto);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new EmpregadoException(mensagemErro);
        }
        return valor;
    }

    private LocalDate validarData(String texto, String mensagemErro) throws EmpregadoException {
        try {
            return Datas.parse(texto);
        } catch (IllegalArgumentException e) {
            throw new EmpregadoException(mensagemErro);
        }
    }

    private LocalDate[] validarIntervalo(String inicioTexto, String fimTexto) throws EmpregadoException {
        LocalDate inicio = validarData(inicioTexto, "Data inicial invalida.");
        LocalDate fim = validarData(fimTexto, "Data final invalida.");
        if (inicio.isAfter(fim)) {
            throw new EmpregadoException("Data inicial nao pode ser posterior aa data final.");
        }
        return new LocalDate[]{inicio, fim};
    }

    /** Traduz UnsupportedOperationException (lancada pelos metodos "default" de Empregado) em EmpregadoException. */
    private interface ChamadaPolimorfica {
        BigDecimal chamar();
    }

    private BigDecimal chamarPolimorfico(ChamadaPolimorfica chamada) throws EmpregadoException {
        try {
            return chamada.chamar();
        } catch (UnsupportedOperationException e) {
            throw new EmpregadoException(e.getMessage());
        }
    }

    // ==================== undo/redo (memento) ====================

    @FunctionalInterface
    private interface Mutacao<T> {
        T executar() throws EmpregadoException;
    }

    private <T> T executarComMemento(Mutacao<T> mutacao) throws EmpregadoException {
        byte[] foto = tirarFoto();
        T resultado = mutacao.executar();
        pilhaUndo.push(foto);
        pilhaRedo.clear();
        return resultado;
    }

    private byte[] tirarFoto() {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ObjectOutputStream saida = new ObjectOutputStream(bytes)) {
            saida.writeObject(empregados);
            saida.writeObject(indiceSindicato);
            saida.writeInt(proximoId);
            saida.flush();
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao tirar foto do estado do sistema.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void restaurarFoto(byte[] foto) {
        try (ObjectInputStream entrada = new ObjectInputStream(new ByteArrayInputStream(foto))) {
            empregados = (Map<String, Empregado>) entrada.readObject();
            indiceSindicato = (Map<String, String>) entrada.readObject();
            proximoId = entrada.readInt();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Falha ao restaurar foto do estado do sistema.", e);
        }
    }

    // ==================== persistencia em disco ====================

    @SuppressWarnings("unchecked")
    private void carregarDoDisco() {
        File arquivo = new File(ARQUIVO_PERSISTENCIA);
        if (!arquivo.exists()) {
            return;
        }
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(arquivo))) {
            empregados = (Map<String, Empregado>) entrada.readObject();
            indiceSindicato = (Map<String, String>) entrada.readObject();
            proximoId = entrada.readInt();
        } catch (IOException | ClassNotFoundException e) {
            // arquivo corrompido ou de outra versao: comeca com o sistema vazio,
            // em vez de derrubar a aplicacao inteira.
            empregados = new LinkedHashMap<>();
            indiceSindicato = new LinkedHashMap<>();
            proximoId = 1;
        }
    }

    private void salvarNoDisco() throws EmpregadoException {
        try (ObjectOutputStream saida = new ObjectOutputStream(new FileOutputStream(ARQUIVO_PERSISTENCIA))) {
            saida.writeObject(empregados);
            saida.writeObject(indiceSindicato);
            saida.writeInt(proximoId);
        } catch (IOException e) {
            throw new EmpregadoException("Nao foi possivel gravar a persistencia: " + e.getMessage());
        }
    }
}
