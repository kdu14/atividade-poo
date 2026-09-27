# WePayU — Sistema de Folha de Pagamento

Projeto da disciplina de Programação Orientada a Objetos (UFAL). Implementa,
por enquanto, o **Milestone 1**: User Stories 1 a 8 (empregados, cartão de
ponto, vendas, taxas de serviço, alteração de cadastro, folha de pagamento e
undo/redo).

## Como rodar os testes de aceitação

Pré-requisitos: JDK 17+ e Maven.

```bash
mvn compile exec:java
```

Isso compila o projeto e roda o EasyAccept contra todos os scripts em
`tests/` (us1.txt a us8.txt, incluindo as continuações `_1`, que testam a
persistência em disco entre uma "sessão" e outra).

Sem Maven instalado, dá pra compilar e rodar na mão:

```bash
javac -encoding UTF-8 -cp lib/easyaccept.jar -d target/classes $(find src/main/java -name "*.java")
java -cp target/classes;lib/easyaccept.jar Main
```

(no Linux/Mac, troque o `;` do classpath por `:`)

## Estrutura

```
src/main/java/
  Main.java                      -- dispara o EasyAccept contra tests/us*.txt
  br/ufal/ic/p2/wepayu/
    Facade.java                  -- so traduz comando de script -> chamada em Sistema
    Sistema.java                 -- toda a validacao e regra de negocio
    excecoes/
      EmpregadoException.java    -- unica excecao checada, usada em toda parte
    modelos/
      Empregado.java             -- classe abstrata: agenda, descontos, undo/redo por foto
      EmpregadoHorista.java
      EmpregadoAssalariado.java
      EmpregadoComissionado.java
      MetodoPagamento.java       -- abstrata; EmMaos / Correios / EmBanco
      CartaoPonto.java / ResultadoVenda.java / TaxaServico.java
      Pagamento.java             -- record com o resultado de UM pagamento
    util/
      Dinheiro.java               -- BigDecimal, truncamento (nao arredondamento!) e formatacao
      Datas.java                  -- parse/validacao de data no formato d/M/yyyy
      Formatador.java             -- monta o relatorio de rodaFolha byte a byte
```

## Decisões de design que valem a pena lembrar (ou defender numa arguição)

- **Nada de `instanceof`.** Onde uma operação só faz sentido para um tipo de
  empregado (bater cartão só para horista, ter comissão só para
  comissionado...), a classe `Empregado` tem uma implementação "padrão" que
  lança erro, e só a subclasse aplicável sobrescreve. Quem chama nunca
  pergunta "que tipo é este?" — só chama o método e deixa o polimorfismo
  decidir. O mesmo vale para `MetodoPagamento` (`recebeEmBanco()` no lugar de
  checar se é uma instância de `EmBanco`).

- **`BigDecimal`, nunca `double`, para dinheiro.** E o arredondamento é
  **truncamento** (`RoundingMode.DOWN`), não o arredondamento comum — isso foi
  confirmado comparando a fórmula contra o relatório oficial (`ok/*.txt`): o
  salário fixo da Suzana Vieira dá 692,3076923..., e o gabarito mostra
  `692,30`, não `692,31`.

- **Memento para undo/redo.** Cada comando que muda o sistema tira uma "foto"
  (serialização em memória) do estado *antes* de rodar; se o comando falhar,
  a foto é descartada; se der certo, ela vai pra pilha de undo e a pilha de
  redo é limpa. Simples de entender, e correto por construção — não precisa
  de uma classe de "comando" por operação.

- **Persistência em disco simples.** O PDF sugere `XMLEncoder`/`XMLDecoder`,
  mas isso exige que toda classe siga a convenção de JavaBean (construtor sem
  argumentos, getter/setter público pra cada campo) — incômodo com uma
  hierarquia de classes abstratas como esta. Optei por serialização Java comum
  (`ObjectOutputStream`), que lida bem com polimorfismo e é bem mais direta.

- **Regra "provisória" da data de contratação.** O enunciado ainda não tem um
  campo de data de contratação em `criarEmpregado`, e pede explicitamente pra
  assumir, só pra estes testes, que assalariados e comissionados foram
  contratados em `1/1/2005` (ver o comentário na User Story 7 do PDF). Isso
  está isolado em `EmpregadoAssalariado.DATA_CONTRATACAO_PADRAO` — quando um
  campo de verdade existir, é o único lugar a mexer.

- **"Rodar a folha duas vezes na mesma data dá o mesmo resultado" é testado
  de verdade** (fim da User Story 7). Cada `Empregado` guarda o último
  pagamento calculado e para qual data foi — se pedirem de novo a mesma data,
  ele devolve o mesmo resultado em vez de recalcular sobre um período vazio
  (o que aconteceria, porque a "data-base" já teria avançado na primeira
  chamada).

## Pendências / próximos passos

- Milestones 2 e 3 (agenda de pagamento customizada — User Stories 9 e 10 — e
  o que mais o professor definir) ainda não foram implementados.
- `docs/desenvolvimento.md` e afins não existem ainda; se o professor exigir
  documentação de arquitetura separada, é o próximo passo natural.
