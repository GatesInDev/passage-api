# Passage API

Backend de ERP e bilhetagem eletrônica para transporte rodoviário de passageiros: cadastro de empresas e frota, linhas e rotas, venda de passagens e controle de acesso por perfil.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F)
![Spring Modulith](https://img.shields.io/badge/Spring%20Modulith-2.1-6DB33F)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791)

## Sumário

- [Módulos](#módulos)
- [Stack](#stack)
- [Como rodar localmente](#como-rodar-localmente)
- [Autenticação e perfis](#autenticação-e-perfis)
- [Endpoints](#endpoints)
- [Arquitetura](#arquitetura)
- [Convenções de código](#convenções-de-código)
- [Testes](#testes)
- [Fluxo de contribuição](#fluxo-de-contribuição)
- [Pontos de atenção e próximos passos](#pontos-de-atenção-e-próximos-passos)

---

## Módulos

| Módulo | Responsabilidade | Situação |
|---|---|---|
| `shared` | `EntityBase`, exceções de negócio e tratamento global de erros | Implementado |
| `iam` | Autenticação JWT, usuários e perfis de acesso (RBAC) | Implementado |
| `fleet` | Empresas transportadoras; ônibus, assentos e motoristas | Empresas com API; frota modelada, sem endpoints |
| `route` | Linhas e rotas, vinculadas à empresa operadora | Implementado |
| `ticket` | Passagens vendidas | Implementado |
| `trip` | Viagens agendadas, horários e alocação de veículos | Planejado |
| `integration` | Integrações assíncronas (SEFAZ/BP-e, ANTT/Monitriip, notificações) | Planejado |

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1 |
| Modularização | Spring Modulith 2.1 |
| Segurança | Spring Security + JWT (`java-jwt`), sessão stateless |
| Persistência | Spring Data JPA (Hibernate) + PostgreSQL 16 |
| Migrações | Flyway (configurado; ver [pontos de atenção](#pontos-de-atenção-e-próximos-passos)) |
| Documentação da API | springdoc-openapi (Swagger UI) |
| Infraestrutura local | Docker Compose: PostgreSQL, Redis 7 e RabbitMQ 3.13 |
| Build | Maven Wrapper (`mvnw`) |
| Utilitários | Lombok |

## Como rodar localmente

### Pré-requisitos

- JDK 21 ou superior
- Docker e Docker Compose

### 1. Subir a infraestrutura

```bash
docker compose up -d
```

| Serviço | Porta | Observação |
|---|---|---|
| PostgreSQL | `5433` | banco `passage_db`, usuário `passage_user` |
| Redis | `6379` | |
| RabbitMQ | `5672` | painel em http://localhost:15672 (`guest`/`guest`) |

### 2. Rodar a aplicação

```bash
./mvnw spring-boot:run
```

No Windows (PowerShell/CMD), use `mvnw.cmd spring-boot:run`.

A API sobe em `http://localhost:8080` e a documentação interativa fica em:

**http://localhost:8080/swagger-ui/index.html**

### 3. Criar o primeiro usuário ADMIN

Ainda não existe um usuário inicial, e o cadastro de usuários exige perfil ADMIN. Por isso, o primeiro administrador é criado direto no banco (a tabela `users` é criada na primeira execução da aplicação).

Gere o hash BCrypt da senha com o `jshell`, usando a biblioteca que o Maven já baixou:

```bash
jshell --class-path ~/.m2/repository/org/springframework/security/spring-security-crypto/7.1.0/spring-security-crypto-7.1.0.jar
```

```java
org.springframework.security.crypto.bcrypt.BCrypt.hashpw("SuaSenhaForte", org.springframework.security.crypto.bcrypt.BCrypt.gensalt())
```

Depois, insira o usuário:

```bash
docker exec -it passage-postgres psql -U passage_user -d passage_db
```

```sql
INSERT INTO users (id, created_at, updated_at, is_active, is_deleted, version, name, email, password, role)
VALUES (gen_random_uuid(), now(), now(), true, false, 0, 'Administrador', 'admin@passage.com.br', '<HASH_GERADO>', 'ADMIN');
```

Com o ADMIN criado, os demais usuários são cadastrados pela própria API (`POST /api/v1/users`).

### Variáveis de ambiente

Todas têm valor padrão para desenvolvimento local.

| Variável | Padrão | Uso |
|---|---|---|
| `DB_HOST` / `DB_PORT` / `DB_NAME` | `localhost` / `5433` / `passage_db` | Conexão com o PostgreSQL |
| `DB_USER` / `DB_PASS` | `passage_user` / `passage_password` | Credenciais do banco |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | Redis |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | `localhost` / `5672` | RabbitMQ |
| `RABBITMQ_USER` / `RABBITMQ_PASS` | `guest` / `guest` | Credenciais do RabbitMQ |
| `API_SECURITY_TOKEN_SECRET` | valor de desenvolvimento | Chave de assinatura do JWT. **Obrigatório definir fora do ambiente local.** |
| `API_SECURITY_TOKEN_EXPIRATION_HOURS` | `24` | Validade do token, em horas |

## Autenticação e perfis

1. Faça login em `POST /api/v1/auth/login` com e-mail e senha.
2. Copie o `accessToken` da resposta.
3. No Swagger, clique em **Authorize** e informe o token. Fora do Swagger, envie o cabeçalho `Authorization: Bearer <token>`.

| Perfil | Descrição |
|---|---|
| `ADMIN` | Acesso irrestrito a todos os módulos |
| `FLEET_MANAGER` | Gestão de empresas, frota e linhas (rotas restritas à própria empresa) |
| `TICKET_SELLER` | Venda, alteração e consulta de passagens |
| `INSPECTOR` | Consulta de passagens para validação de embarque |
| `CUSTOMER` | Passageiro: consulta de rotas |

## Endpoints

Todos os endpoints, exceto o login e o Swagger, exigem token.

### Autenticação — `/api/v1/auth`

| Método | Rota | Acesso |
|---|---|---|
| `POST` | `/login` | Público |

### Usuários — `/api/v1/users`

| Método | Rota | Acesso |
|---|---|---|
| `POST` | `/` | ADMIN |
| `GET` | `/` e `/{id}` | ADMIN |
| `PUT` | `/{id}` | ADMIN |
| `DELETE` | `/{id}` (exclusão lógica) | ADMIN |

### Empresas — `/api/v1/companies`

| Método | Rota | Acesso |
|---|---|---|
| `POST` | `/` | ADMIN |
| `GET` | `/{id}` | ADMIN, FLEET_MANAGER |
| `PATCH` | `/{id}` | ADMIN, FLEET_MANAGER |

### Rotas — `/api/v1/routes`

| Método | Rota | Acesso |
|---|---|---|
| `POST` | `/` | ADMIN, FLEET_MANAGER¹ |
| `GET` | `/` e `/{id}` | Qualquer usuário autenticado |
| `PUT` | `/{id}` | ADMIN, FLEET_MANAGER¹ |
| `DELETE` | `/{id}` (exclusão lógica) | ADMIN, FLEET_MANAGER¹ |

¹ Cada rota pertence a uma empresa. O FLEET_MANAGER cria rotas sempre na própria empresa e só altera ou exclui rotas dela; o ADMIN informa a empresa no cadastro (`companyUuid`) e pode atuar em qualquer rota.

### Passagens — `/api/v1/tickets`

| Método | Rota | Acesso |
|---|---|---|
| `POST` | `/` | ADMIN, TICKET_SELLER |
| `GET` | `/{id}` | ADMIN, TICKET_SELLER, INSPECTOR |
| `GET` | `/` | ADMIN, TICKET_SELLER |
| `PUT` | `/{id}` | ADMIN, TICKET_SELLER |
| `DELETE` | `/{id}` (exclusão lógica) | ADMIN |

## Arquitetura

O projeto é um **monólito modular**: uma única aplicação, dividida em módulos de domínio independentes, com as fronteiras verificáveis pelo **Spring Modulith**.

```text
br.com.passage.api
├── shared                  # EntityBase, BusinessException, tratamento global de erros
├── iam                     # IamApi e CurrentUser (API pública)
│   ├── enums               # UserRole
│   └── internal            # autenticação, JWT, usuários
├── fleet                   # FleetApi
│   └── internal
├── route                   # RouteApi
│   └── internal
└── ticket                  # TicketApi
    └── internal
```

Cada módulo de negócio segue a mesma organização interna:

```text
internal
├── domain
│   ├── entities            # entidades JPA (regras de negócio ficam aqui)
│   └── enums
├── dto                     # requests e responses (records)
├── repository              # interfaces Spring Data
├── service                 # um service por caso de uso
└── web                     # controllers REST
```

### Regras entre módulos

1. **O pacote raiz do módulo é a API pública** (ex.: `IamApi`, `CurrentUser`). Todo o resto fica em `.internal`.
2. **Nunca importe classes de `.internal` de outro módulo.**
3. **Subpacote que precisa ser público** recebe um `package-info.java` com `@NamedInterface`, como em `shared/domain/exceptions`:
   ```java
   @org.springframework.modulith.NamedInterface("domain-exceptions")
   package br.com.passage.api.shared.domain.exceptions;
   ```
4. **Relacionamentos entre módulos são feitos por UUID**, não por `@ManyToOne` (ex.: `User.companyUuid`, `Route.companyUuid`, `Ticket.routeId`).
5. **Usuário logado:** use `IamApi.currentUser()`, que devolve id, perfil e empresa. Nunca confie em empresa ou usuário enviados no corpo da requisição.

## Convenções de código

### Entidades

Toda entidade herda de `EntityBase` (`shared`), que fornece:

| Campo | Descrição |
|---|---|
| `id` (`UUID`) | Identificador público, usado nos endpoints e DTOs |
| `createdAt` / `updatedAt` | Preenchidos automaticamente (JPA Auditing) |
| `isActive` | Se o registro está ativo |
| `isDeleted` | Exclusão lógica |
| `version` | Controle de concorrência otimista (`@Version`) |

- Não use `@Data` do Lombok em entidades: `equals` e `hashCode` usam apenas o `id`.
- Regras de negócio e validações de estado ficam na entidade (ex.: `Route.belongsTo(companyUuid)`); os services só orquestram.

### Exclusão lógica

Registros nunca são apagados do banco: a exclusão marca `isDeleted = true` e `isActive = false`. Toda consulta (listagem **e** busca por id) deve desconsiderar registros excluídos.

### DTOs

- São `record`s. Os de entrada carregam a validação (`@NotBlank`, `@Size`, `@Pattern`…) e a documentação (`@Schema`).
- A conversão de entidade para resposta fica no próprio DTO, em um método estático `from` (ex.: `RouteResponse.from(route)`).

### Services

- Um service por caso de uso, com um método `execute` (ex.: `CreateRouteService`, `FindRouteService`).
- `@Transactional` do Spring (`org.springframework.transaction.annotation`), com `readOnly = true` nas consultas.

### Erros

As respostas de erro seguem o formato `ProblemDetail` (RFC 7807), via `GlobalExceptionHandler` (`shared`).

| Situação | Como sinalizar | HTTP |
|---|---|---|
| Regra de negócio violada (ex.: código duplicado) | `throw new BusinessException("...")` | 409 |
| Dados de entrada inválidos | `@Valid` no controller | 400 |
| Usuário sem permissão | `@PreAuthorize` ou `AccessDeniedException` | 403 |

### Permissões

- Declare o acesso em cada endpoint com `@PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")`. Use o nome do perfil sem o prefixo `ROLE_`.
- Restrições que dependem do dado (ex.: "só a própria empresa") ficam no service, lançando `AccessDeniedException`.

## Testes

```bash
./mvnw test
```

Executa a inicialização completa da aplicação (`ApiApplicationTests`) e os testes da API pública do `iam` (`IamApiTests`). A infraestrutura do Docker precisa estar no ar.

## Fluxo de contribuição

1. **Branch** a partir da `develop`, com o padrão `tipo/escopo`:
   `feature/routes-2`, `fix/exception-handler`, `docs/update-readme`, `chore/docker`.
2. **Commits** no padrão Conventional Commits, com o módulo como escopo:
   `feat(route): adiciona exclusão lógica de rotas`, `fix(shared): ...`, `docs: ...`.
3. **Antes do PR:** atualize a branch com a `develop` (`git merge origin/develop`), rode `./mvnw clean test` e teste os endpoints no Swagger.
4. **Pull request para a `develop`**, com resumo, o que mudou e como testar.

## Pontos de atenção e próximos passos

- **Migrações:** as tabelas ainda são criadas pelo Hibernate (`ddl-auto=update`); o Flyway está configurado, mas sem migrations.
- **Usuário inicial:** não há seed de ADMIN (ver [passo 3](#3-criar-o-primeiro-usuário-admin)).
- **Códigos HTTP:** recurso não encontrado retorna 409 (via `BusinessException`) em vez de 404, e requisições sem token retornam 403 em vez de 401.
- **Verificação de módulos:** os testes ainda não executam `ApplicationModules.verify()` do Spring Modulith. O pacote `iam.enums` precisa de `@NamedInterface` para que o `UserRole` seja considerado público.
- **Infraestrutura:** Redis e RabbitMQ já estão provisionados, mas ainda não são usados no código.
- **Módulos planejados:** `trip` e `integration`.
