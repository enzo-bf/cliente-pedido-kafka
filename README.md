# cliente-pedido-kafka

API REST em Spring Boot para cadastro de clientes e pedidos, com processamento
assíncrono dos pedidos via Apache Kafka: o pedido é criado de forma síncrona e o
cálculo do desconto e o registro no histórico acontecem no consumidor do evento.

## Stack

- Java 17 e Spring Boot 4.0.8 (Web MVC, Validation, Data JPA, Kafka)
- Banco H2 em memória (console em `/h2-console`)
- Apache Kafka (broker local via Docker Compose)
- Lombok
- Testes: JUnit 5, Mockito, AssertJ, MockMvcTester e Embedded Kafka

## Arquitetura

```
controller  ->  service  ->  repository  ->  H2
                   |
                   v
             PedidoEventProducer  --(topico pedidos.criados)-->  PedidoEventConsumer
                                                                        |
                                                                        v
                                                  PedidoService.processarPedidoCriado
                                                  (aplica desconto + grava histórico)
```

- `entity`: `Cliente`, `Pedido`, `HistoricoPedido`
- `dto/request`, `dto/response`: contratos de entrada e saída da API
- `event`: `PedidoCriadoEvent`, mensagem trafegada no Kafka
- `messaging`: produtor e consumidor Kafka
- `config`: criação do tópico e propriedades `app.kafka.topic.*`
- `exception`: exceções de domínio e `GlobalExceptionHandler`

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

| Método | Rota             | Descrição              | Respostas          |
| ------ | ---------------- | ---------------------- | ------------------ |
| POST   | `/clientes`      | Cadastra um cliente    | 201, 400, 409      |
| GET    | `/clientes/{id}` | Busca cliente por id   | 200, 404           |

```bash
curl -X POST http://localhost:8080/clientes \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Enzo","cpf":"12345678901","email":"enzo@email.com"}'
```

### Pedidos

| Método | Rota                        | Descrição                          | Respostas     |
| ------ | --------------------------- | ---------------------------------- | ------------- |
| POST   | `/pedidos`                  | Cria pedido e publica o evento     | 201, 400, 404 |
| GET    | `/pedidos/{id}`             | Busca pedido por id                | 200, 404      |
| GET    | `/pedidos?clienteId={id}`   | Lista pedidos de um cliente        | 200, 404      |
| GET    | `/pedidos/{id}/historico`   | Lista o histórico de processamento | 200, 404      |

```bash
curl -X POST http://localhost:8080/pedidos \
  -H 'Content-Type: application/json' \
  -d '{"clienteId":1,"descricao":"Notebook","valor":1000.00}'
```

A resposta do POST traz `desconto` e `valorFinal` nulos; após o consumo do evento
`pedidos.criados` esses campos são preenchidos e uma entrada
`PEDIDO_PROCESSADO` passa a existir no histórico.

### Erros

Todos os erros seguem o contrato `ErroResponse`:

```json
{ "timestamp": "2025-01-01T10:00:00", "status": 404, "message": "Pedido não encontrado com o ID: 99" }
```

## Configuração

| Propriedade                       | Padrão            | Descrição                        |
| --------------------------------- | ----------------- | -------------------------------- |
| `spring.kafka.bootstrap-servers`  | `localhost:9092`  | Endereço do broker               |
| `spring.kafka.consumer.group-id`  | `cliente-pedido-kafka` | Consumer group              |
| `app.kafka.topic.pedidos-criados` | `pedidos.criados` | Tópico dos eventos de pedido     |
| `app.kafka.topic.particoes`       | `1`               | Partições do tópico              |
| `app.kafka.topic.replicas`        | `1`               | Réplicas do tópico               |

## Testes

```bash
./mvnw test
```

Os testes não exigem Kafka nem banco externos: o fluxo assíncrono é validado com
`@EmbeddedKafka` e a persistência com H2 em memória.
