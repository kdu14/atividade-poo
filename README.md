# WePayU — Sistema de Folha de Pagamento

Projeto da disciplina de Programação Orientada a Objetos (UFAL). Implementa
até o **Milestone 2**: User Stories 1 a 10 (empregados, cartão de ponto,
vendas, taxas de serviço, alteração de cadastro, folha de pagamento,
undo/redo, agenda de pagamento e criação de novas agendas).

## Como rodar os testes de aceitação

Pré-requisitos: JDK 17+ e Maven.

```bash
mvn compile exec:java
```

Isso compila o projeto e roda o EasyAccept contra os scripts oficiais
(`tests/us1.txt` a `tests/us10_1.txt`, incluindo as continuações `_1`, que
testam a persistência em disco entre uma execução e outra) e, por último,
contra os nossos testes extras (`tests/extras/agendas.txt`). O `wepayu.dat`
é apagado antes do primeiro script, para toda execução começar do zero.

Sem Maven instalado, dá pra compilar e rodar na mão:

```bash
javac -encoding UTF-8 -cp lib/easyaccept.jar -d target/classes $(find src/main/java -name "*.java")
java -cp "target/classes;lib/easyaccept.jar" br.ufal.ic.p2.wepayu.Main
```

(no Linux/Mac, troque o `;` do classpath por `:`)

### Resultado esperado

Todos os scripts passam, com **uma exceção que não depende do código**: a
linha 100 do `us9.txt` tem um erro de digitação no próprio script — falta o
espaço em `expect 1292,30totalFolha data=14/1/2005`. O EasyAccept lê
`data=14/1/2005` como nome de comando e acusa `Unknown command`. O valor em
si é calculado corretamente: a linha 102, que espera os mesmos 1292,30 no
pagamento seguinte (28/1), passa.

## Estrutura

```
src/main/java/br/ufal/ic/p2/wepayu/
  Main.java                 -- dispara o EasyAccept contra os scripts
  Facade.java               -- so repassa cada comando ao servico do assunto
  servicos/                 -- regras de negocio, um servico por assunto
    Sessao                  -- estado da execucao; confere "nada depois de encerrarSistema"
    ServicoDoSistema        -- zerar, encerrar, undo, redo
    ServicoDeEmpregados     -- criar, remover, consultar
    ServicoDeAlteracao      -- as variacoes de alteraEmpregado
    ServicoDoSindicato      -- filiacao e taxas de servico
    ServicoDeLancamentos    -- cartoes de ponto e vendas
    ServicoDaFolha          -- totalFolha e rodaFolha
    ServicoDeAgendas        -- criarAgendaDePagamentos
  persistencia/
    Repositorio             -- todo o estado (empregados, sindicato, ids, agendas)
    ArquivoDeDados          -- leitura e gravacao do wepayu.dat
    Historico               -- undo/redo por memento, com restauracao em caso de erro
  modelos/                  -- Empregado (abstrata) e subclasses, MembroSindicato,
                               metodos de pagamento, lancamentos, Pagamento
    agenda/                 -- AgendaPagamento (abstrata), semanal e mensais
  fabricas/                 -- unico lugar que cria empregados e agendas
  validacao/Entrada         -- converte e valida o texto dos comandos
  relatorio/RelatorioDaFolha -- monta o arquivo de rodaFolha
  excecoes/                 -- WePayUException e uma subclasse por tipo de erro
  util/                     -- Dinheiro, Horas, Datas, Periodo, Exigir
```

## Decisões de projeto

- **Um serviço por assunto.** A `Facade` só encaminha; cada serviço cuida de
  um assunto e todos compartilham a mesma `Sessao`. O acesso ao estado passa
  sempre pela `Sessao`, então a regra "nada depois de `encerrarSistema`" é
  conferida num lugar só.

- **Undo/redo por memento, com transação.** Antes de cada comando que muda o
  sistema, o `Historico` guarda uma foto (serialização em memória) do
  `Repositorio`. Se o comando falhar no meio, a foto é **restaurada** e o
  erro segue para quem chamou; o comando que falha não entra no histórico.
  As agendas criadas também ficam no `Repositorio`, então o undo, o
  `zerarSistema` e a persistência valem para elas sem código extra.

- **Uma exceção por tipo de erro.** Todas descendem de `WePayUException`. As
  mensagens são exatamente as que os testes esperam.

- **Agenda de pagamento.** `AgendaPagamento` responde três perguntas: se a
  data é dia de pagamento, onde começa o período pago e quanto do salário
  mensal cabe no pagamento (o mensal inteiro, ou `salario*12*N/52` numa agenda
  de N semanas). As regras fixas do Milestone 1 viraram as agendas padrão:
  `semanal 5` (horista), `mensal $` (assalariado) e `semanal 2 5`
  (comissionado). As agendas semanais contam as semanas a partir de
  1/1/2005, a data de contratação que o enunciado manda assumir: por isso
  `semanal 2 5` paga primeiro em 14/1/2005 e `semanal 52 1` em 26/12/2005.

- **Período pago.** Vai do dia seguinte ao pagamento anterior (pela agenda)
  até o dia do pagamento, inclusive, e nunca começa antes da contratação (o
  horista é contratado no dia do primeiro cartão). Assim, `totalFolha` em
  dias seguidos não acumula períodos já cobertos.

- **Dívida sindical.** O contracheque nunca fica negativo. O que não cabe
  nos descontos vira um saldo do `MembroSindicato`, cobrado no próximo
  pagamento junto com a taxa diária e as taxas de serviço (Obs. 1 da US7).

- **Troca de tipo.** O empregado mantém nome, endereço, método de pagamento
  e filiação ao sindicato (com taxas e dívida). A agenda escolhida também
  fica; se ele estava na agenda padrão do tipo antigo, passa para a padrão do
  tipo novo.

- **`BigDecimal`, nunca `double`, para dinheiro**, com **truncamento**
  (`RoundingMode.DOWN`), conferido contra o gabarito oficial: o fixo
  quinzenal de um salário de 1500 dá 692,3076923..., e o gabarito mostra
  `692,30`.

- **Persistência com serialização Java.** O PDF sugere `XMLEncoder`, mas ele
  exige o padrão JavaBean (construtor vazio e setter público para cada campo)
  em toda a hierarquia, abrindo mão do encapsulamento. Um arquivo ilegível é
  erro (`ArquivoException`), e não um sistema vazio em silêncio.

- **Objetos sempre válidos.** Os serviços validam a entrada com a mensagem de
  negócio; os modelos ainda recusam valores inválidos (nome vazio, salário
  negativo...), para nunca ficarem num estado inconsistente.

## Testes extras

`tests/extras/agendas.txt` cobre o que os scripts oficiais não cobrem:
agendas personalizadas para horista e comissionado, troca de tipo, undo/redo
de agenda, comando que falha sem alterar o estado, o id não consumido por um
`criarEmpregado` inválido e a dívida sindical do horista (com os relatórios
esperados em `tests/extras/ok/`).
