# Cliente Pedido Kafka

Sistema corporativo de cadastro de clientes e pedidos, com histórico de status, publicação/consumo de eventos Kafka e interface React.

## Arquitetura

- **API REST** em Spring Boot 4 (Java 17), camada Controller → Service → Repository.
- **MySQL** como banco principal, versionado com **Flyway**.
- **Kafka** para `PedidoCriadoEvent` e `PedidoStatusAlteradoEvent`.
- **React + TypeScript + Vite** para o front-end corporativo.
- Padrões: DTO, Service Layer, Repository, Builder (Lombok), Strategy (transição de status) e Global Exception Handler.

```
frontend/  → SPA (porta 5173)
backend   → /api (porta 8080)
MySQL      → 3306
Kafka      → 9092
Zookeeper  → 2181
```

## Tecnologias

Backend: Java 17, Spring Boot, Spring Data JPA, Hibernate, Validation, Kafka, Flyway, Lombok, springdoc-openapi, JUnit 5, Mockito.

Frontend: React, TypeScript, Vite, TailwindCSS, Axios, React Query, React Hook Form, Zod, Lucide, Vitest.

## Execução local

Pré-requisitos: JDK 17, Maven, Node 22+, Docker (MySQL + Kafka).

1. Suba a infraestrutura:

```bash
docker compose up mysql zookeeper kafka -d
```

2. Backend:

```bash
mvn spring-boot:run
```

API: http://localhost:8080/api  
Swagger: http://localhost:8080/api/swagger-ui.html

3. Frontend:

```bash
cd frontend
npm install
npm run dev
```

Interface: http://localhost:5173

## Docker (stack completa)

```bash
docker compose up --build
```

- Front: http://localhost:5173
- API: http://localhost:8080/api
- Swagger: http://localhost:8080/api/swagger-ui.html

## Testes

```bash
mvn test
cd frontend && npm test
```

## Endpoints

| Método | Caminho | Descrição |
| --- | --- | --- |
| POST | `/api/clientes` | Cadastrar cliente |
| GET | `/api/clientes` | Listar/filtrar (nome, cpf, email, dataCadastroInicio, dataCadastroFim) |
| GET | `/api/clientes/{id}` | Buscar cliente |
| PUT | `/api/clientes/{id}` | Atualizar cliente |
| DELETE | `/api/clientes/{id}` | Excluir cliente |
| POST | `/api/pedidos` | Cadastrar pedido |
| GET | `/api/pedidos` | Listar/filtrar |
| GET | `/api/pedidos/{id}` | Buscar pedido |
| PUT | `/api/pedidos/{id}` | Atualizar pedido |
| PATCH | `/api/pedidos/{id}/status` | Alterar status |
| POST | `/api/pedidos/{id}/cancelar` | Cancelar |
| GET | `/api/pedidos/{id}/historico` | Histórico de status |
| GET | `/api/dashboard` | Métricas |

Paginação padrão Spring (`page`, `size`, `sort`).

## Fluxo Kafka

1. `POST /api/pedidos` persiste o pedido com status `CRIADO`, grava histórico e publica **PedidoCriadoEvent** no tópico `pedido-criado`.
2. `PATCH /api/pedidos/{id}/status` ou cancelamento valida a transição (Strategy), grava histórico e publica **PedidoStatusAlteradoEvent** no tópico `pedido-status-alterado`.
3. O consumer registra log estruturado (`event=... pedidoId=...`).

Transições permitidas:

- CRIADO → PROCESSANDO, CANCELADO
- PROCESSANDO → APROVADO, REJEITADO, CANCELADO
- APROVADO → FINALIZADO, CANCELADO
- REJEITADO → CANCELADO
- CANCELADO / FINALIZADO → nenhuma

## Regras de negócio preservadas

- CPF e e-mail únicos, nome/CPF/e-mail obrigatórios.
- Pedido exige cliente existente, valor positivo e desconto entre 0% e 20% (regra já existente no projeto).
- Cliente com pedidos vinculados não pode ser excluído.
