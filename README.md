# cliente-pedido-kafka

API REST em Spring Boot para cadastro de clientes e pedidos, com processamento
assíncrono dos pedidos via Apache Kafka: o pedido é criado (ou atualizado) de
forma síncrona e o cálculo do desconto e o registro no histórico acontecem no
consumidor do evento correspondente.

## Stack

- Java 17 e Spring Boot 4.0.8 (Web MVC, Validation, Data JPA, Kafka)
- Banco H2 em memória (console em `/h2-console`)
- Apache Kafka (broker local via Docker Compose)
- Lombok
- Testes: JUnit 5, Mockito, AssertJ, MockMvcTester e Embedded Kafka

## Arquitetura

```
controller  ->  service  ->  repository (+ Specification)  ->  H2
                   |
                   v
             PedidoEventProducer
                   |
                   +--(tópico pedidos.criados)----->  PedidoEventConsumer -> processarPedidoCriado
                   |                                                          (aplica desconto,
                   |                                                           grava histórico
                   |                                                           PEDIDO_PROCESSADO)
                   |
                   +--(tópico pedidos.atualizados)-->  PedidoEventConsumer -> processarPedidoAtualizado
                                                                               (recalcula desconto,
                                                                                grava histórico
                                                                                PEDIDO_ATUALIZADO)
```

- `entity`: `Cliente`, `Pedido`, `HistoricoPedido`
- `dto/request`, `dto/response`: contratos de entrada e saída da API
- `event`: `PedidoCriadoEvent`, `PedidoAtualizadoEvent`, mensagens trafegadas no Kafka
- `messaging`: produtor e consumidor Kafka
- `config`: criação dos tópicos e propriedades `app.kafka.topic.*`
- `repository/specification`: `ClienteSpecification` e `PedidoSpecification`, usadas
  para combinar filtros opcionais nas listagens
- `exception`: exceções de domínio e `GlobalExceptionHandler`

### Atualização de pedidos (PUT /pedidos/{id})

Assim como no cadastro, a atualização não recalcula o desconto na hora: ela
salva os novos dados do pedido, zera `desconto`/`valorFinal` e publica o
evento `PEDIDO_ATUALIZADO` no tópico `pedidos.atualizados`. O consumidor
recalcula o desconto a partir do novo valor e grava uma nova entrada de
histórico. Isso mantém o mesmo padrão assíncrono já usado na criação e
garante que o valor final do pedido nunca fique desatualizado em relação ao
último valor informado.

### Regras de desconto

Aplicadas no processamento assíncrono (`CalculadoraDesconto`), com arredondamento
`HALF_UP` em duas casas:

| Valor do pedido | Desconto |
| --------------- | -------- |
| >= 1000         | 10%      |
| >= 500 e < 1000 | 5%       |
| < 500           | 0%       |

O processamento é idempotente: eventos repetidos para um pedido que já possui
`valorFinal` são ignorados.

## Como executar

1. Suba o broker Kafka:

```bash
docker compose up -d
```

2. Execute a aplicação:

```bash
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`.

## Endpoints

### Clientes

| Método | Rota             | Descrição                              | Respostas          |
| ------ | ---------------- | --------------------------------------- | ------------------ |
| POST   | `/clientes`      | Cadastra um cliente                    | 201, 400, 409      |
| GET    | `/clientes/{id}` | Busca cliente por id                    | 200, 404           |
| GET    | `/clientes`      | Lista clientes (paginado, com filtros) | 200                |
| PUT    | `/clientes/{id}` | Atualiza um cliente                    | 200, 400, 404, 409 |
| DELETE | `/clientes/{id}` | Exclui um cliente                      | 204, 404           |

```bash
curl -X POST http://localhost:8080/clientes \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Enzo","cpf":"12345678901","email":"enzo@email.com"}'

curl -X PUT http://localhost:8080/clientes/1 \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Enzo Atualizado","cpf":"12345678901","email":"enzo@email.com"}'

curl -X DELETE http://localhost:8080/clientes/1

# listagem com paginação, ordenação e filtros (todos opcionais e combináveis)
curl "http://localhost:8080/clientes?page=0&size=10&sort=nome,asc"
curl "http://localhost:8080/clientes?cpf=12345678900"
curl "http://localhost:8080/clientes?nome=Joao"
```

### Pedidos

| Método | Rota                        | Descrição                                       | Respostas     |
| ------ | --------------------------- | ------------------------------------------------ | ------------- |
| POST   | `/pedidos`                  | Cria pedido e publica `PEDIDO_CRIADO`            | 201, 400, 404 |
| GET    | `/pedidos/{id}`             | Busca pedido por id                              | 200, 404      |
| GET    | `/pedidos`                  | Lista pedidos (paginado, com filtros)            | 200           |
| PUT    | `/pedidos/{id}`             | Atualiza pedido e publica `PEDIDO_ATUALIZADO`    | 200, 400, 404 |
| DELETE | `/pedidos/{id}`             | Exclui um pedido                                 | 204, 404      |
| GET    | `/pedidos/{id}/historico`   | Lista o histórico de um pedido específico        | 200, 404      |
| GET    | `/historico-pedidos`        | Lista histórico de todos os pedidos (paginado, com filtro por tipo de evento) | 200 |

```bash
curl -X POST http://localhost:8080/pedidos \
  -H 'Content-Type: application/json' \
  -d '{"clienteId":1,"descricao":"Notebook","valor":1000.00}'

curl -X PUT http://localhost:8080/pedidos/10 \
  -H 'Content-Type: application/json' \
  -d '{"clienteId":1,"descricao":"Notebook Pro","valor":1200.00}'

curl -X DELETE http://localhost:8080/pedidos/10

# filtros combináveis por cliente e/ou faixa de valor, com paginação e ordenação
curl "http://localhost:8080/pedidos?clienteId=10"
curl "http://localhost:8080/pedidos?valorMin=100"
curl "http://localhost:8080/pedidos?valorMax=1000"
curl "http://localhost:8080/pedidos?valorMin=100&valorMax=500"
curl "http://localhost:8080/pedidos?page=0&size=10&sort=dataCriacao,desc"

# histórico filtrado por tipo de evento
curl "http://localhost:8080/historico-pedidos?tipoEvento=PEDIDO_PROCESSADO"
curl "http://localhost:8080/historico-pedidos?tipoEvento=PEDIDO_ATUALIZADO&page=0&size=10"
```

A resposta do POST e do PUT traz `desconto` e `valorFinal` nulos; após o
consumo do evento correspondente (`pedidos.criados` ou `pedidos.atualizados`)
esses campos são preenchidos e uma entrada (`PEDIDO_PROCESSADO` ou
`PEDIDO_ATUALIZADO`) passa a existir no histórico.

### Paginação e ordenação

Os endpoints de listagem introduzidos nesta versão (`GET /clientes`,
`GET /pedidos` e `GET /historico-pedidos`) aceitam os parâmetros padrão do
Spring Data (`Pageable`):

- `page`: número da página, começando em 0 (padrão: `0`)
- `size`: tamanho da página (padrão: `10`)
- `sort`: campo e direção, no formato `campo,asc` ou `campo,desc`; pode ser
  repetido para ordenar por múltiplos campos

A resposta é a página do Spring Data serializada como JSON, com o conteúdo em
`content` e metadados de paginação (`totalElements`, `totalPages`, `number`,
`size` etc.), por exemplo:

```json
{
  "content": [ { "id": 1, "nome": "Enzo", "...": "..." } ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

O endpoint `GET /pedidos/{id}/historico` (histórico de um pedido específico)
foi mantido como já estava, retornando uma lista simples, para não quebrar o
contrato já existente; para navegar de forma paginada pelo histórico de todos
os pedidos, use `GET /historico-pedidos`.

### Erros

Todos os erros seguem o contrato `ErroResponse`:

```json
{ "timestamp": "2025-01-01T10:00:00", "status": 404, "message": "Pedido não encontrado com o ID: 99" }
```

## Configuração

| Propriedade                           | Padrão                | Descrição                          |
| -------------------------------------- | ---------------------- | ----------------------------------- |
| `spring.kafka.bootstrap-servers`      | `localhost:9092`       | Endereço do broker                 |
| `spring.kafka.consumer.group-id`      | `cliente-pedido-kafka` | Consumer group                     |
| `app.kafka.topic.pedidos-criados`     | `pedidos.criados`      | Tópico dos eventos de pedido criado |
| `app.kafka.topic.pedidos-atualizados` | `pedidos.atualizados`  | Tópico dos eventos de pedido atualizado |
| `app.kafka.topic.particoes`           | `1`                    | Partições dos tópicos              |
| `app.kafka.topic.replicas`            | `1`                    | Réplicas dos tópicos               |

## Testes

```bash
./mvnw test
```

Os testes não exigem Kafka nem banco externos: o fluxo assíncrono é validado com
`@EmbeddedKafka` e a persistência com H2 em memória. Além dos testes de
controller e service (unitários, com Mockito), há testes de integração com
`@SpringBootTest` que exercitam os filtros (`ClienteSpecification`,
`PedidoSpecification`) e a paginação diretamente contra o H2, e um teste de
ponta a ponta para o fluxo de atualização de pedido via Kafka
(`PedidoEventIntegrationTest`).
