# Passage API

Backend de ERP e bilhetagem eletrônica para transporte rodoviário de passageiros: cadastro de empresas e frota, linhas e rotas, viagens agendadas, venda de passagens e controle de acesso por perfil.

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
| `fleet` | Empresas transportadoras, ônibus, assentos e motoristas, com isolamento por empresa | CRUDs implementados |
| `route` | Linhas e rotas, vinculadas à empresa operadora | Implementado |
| `ticket` | Passagens vendidas | Implementado |
| `trip` | Viagens agendadas de uma rota: horários, ônibus, motorista, preço e situação | Implementado |
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
| `FLEET_MANAGER` | Consulta e gestão da própria empresa e frota; criação, alteração e exclusão de rotas e viagens da própria empresa |
| `TICKET_SELLER` | Venda, alteração e consulta de passagens |
| `INSPECTOR` | Consulta de passagens para validação de embarque |
| `CUSTOMER` | Passageiro: consulta de rotas e viagens |

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
| `GET` | `/` e `/{id}` | ADMIN, FLEET_MANAGER (somente a própria empresa) |
| `PATCH` | `/{id}` | ADMIN, FLEET_MANAGER |
| `DELETE` | `/{id}` (exclusão lógica) | ADMIN |

### Ônibus — `/api/v1/buses`

| Método | Rota | Acesso |
|---|---|---|
| `POST` | `/` | ADMIN, FLEET_MANAGER |
| `GET` | `/` e `/{id}` | ADMIN, FLEET_MANAGER |
| `PUT` | `/{id}` | ADMIN, FLEET_MANAGER |
| `DELETE` | `/{id}` (exclusão lógica) | ADMIN, FLEET_MANAGER |

O cadastro recebe `code`, `model`, `manufacturer`, `licensePlate` e `companyUuid`. O ADMIN deve informar uma empresa existente e não excluída; para o gerente, a empresa vem do usuário autenticado, independentemente do valor enviado. A atualização recebe os campos cadastrais e `isActive`, sem transferir a empresa. A capacidade é calculada pelos assentos ativos e não excluídos.

### Motoristas — `/api/v1/drivers`

| Método | Rota | Acesso |
|---|---|---|
| `POST` | `/` | ADMIN, FLEET_MANAGER |
| `GET` | `/` e `/{id}` | ADMIN, FLEET_MANAGER |
| `PUT` | `/{id}` | ADMIN, FLEET_MANAGER |
| `DELETE` | `/{id}` (exclusão lógica) | ADMIN, FLEET_MANAGER |

O cadastro recebe `name`, `licenseNumber`, `licenseCategory` (opcional) e `companyUuid`, com a mesma regra de empresa dos ônibus. A atualização recebe os dados cadastrais e `isActive`, sem transferir a empresa.

### Assentos — `/api/v1/buses/{busId}/seats`

| Método | Rota | Acesso |
|---|---|---|
| `POST` | `/` | ADMIN, FLEET_MANAGER |
| `GET` | `/` e `/{id}` | ADMIN, FLEET_MANAGER |
| `PUT` | `/{id}` | ADMIN, FLEET_MANAGER |
| `DELETE` | `/{id}` (exclusão lógica) | ADMIN, FLEET_MANAGER |

O cadastro recebe `seatNumber`, `row`, `column`, `position`, `quality` e `isAccessible`; a atualização também exige `isActive`. Fileira e coluna devem ser positivas. `position` aceita `WINDOW`, `AISLE` ou `MIDDLE`; `quality` aceita `STANDARD`, `COMFORT` ou `PREMIUM`. O número e a combinação de fileira e coluna são únicos dentro do ônibus.

### Isolamento e exclusão no fleet

- O gerente só lista, consulta, altera ou exclui ônibus, motoristas e assentos da própria empresa. As consultas de ônibus e motoristas aplicam o tenant no banco; assentos exigem acesso ao ônibus da URL e pertencimento a esse mesmo ônibus.
- Um gerente sem empresa é recusado com 403; outros perfis também não acessam os CRUDs de fleet. O ADMIN pode operar entre empresas.
- Buscar ou alterar ônibus, motorista ou assento fora do escopo retorna 404. Acesso direto a outra empresa retorna 403, preservando a regra de autorização de empresas.
- Registros excluídos não retornam nas consultas. Excluir uma empresa oculta sua frota; excluir um ônibus oculta seus assentos. Os filhos permanecem armazenados.
- CNPJ, placa e CNH continuam únicos no sistema; número e posição do assento continuam reservados no ônibus após exclusão lógica, conforme as restrições existentes no banco.
- Rotas e viagens seguem o mesmo isolamento para o FLEET_MANAGER, mas continuam visíveis a todos os demais perfis autenticados (ver abaixo).

### Rotas — `/api/v1/routes`

| Método | Rota | Acesso |
|---|---|---|
| `POST` | `/` | ADMIN, FLEET_MANAGER¹ |
| `GET` | `/` e `/{id}` | Qualquer usuário autenticado¹ |
| `PUT` | `/{id}` | ADMIN, FLEET_MANAGER¹ |
| `DELETE` | `/{id}` (exclusão lógica) | ADMIN, FLEET_MANAGER¹ |

¹ Cada rota pertence a uma empresa. O FLEET_MANAGER cria rotas sempre na própria empresa e só lista, consulta, altera ou exclui rotas dela: rotas de outra empresa retornam 404. O ADMIN informa a empresa no cadastro (`companyUuid`) e atua em qualquer rota. Os demais perfis consultam as rotas de todas as empresas.

### Viagens — `/api/v1/trips`

| Método | Rota | Acesso |
|---|---|---|
| `POST` | `/` | ADMIN, FLEET_MANAGER² |
| `GET` | `/` e `/{id}` | Qualquer usuário autenticado² |
| `PUT` | `/{id}` | ADMIN, FLEET_MANAGER² |
| `DELETE` | `/{id}` (exclusão lógica) | ADMIN, FLEET_MANAGER² |

² A viagem é uma saída de uma rota em data e hora definidas, e herda a empresa da rota (`routeId`). O FLEET_MANAGER só cria viagens em rotas da própria empresa (403 em rota de outra) e só lista, consulta, altera ou exclui viagens dela: viagens de outra empresa retornam 404. Os demais perfis consultam as viagens de todas as empresas. Toda viagem nasce com situação `SCHEDULED`; a chegada deve ser posterior à partida, e o código e a rota não mudam depois do cadastro.

| Situação (`status`) | Significado |
|---|---|
| `SCHEDULED` | Agendada, ainda não iniciada |
| `BOARDING` | Embarque em andamento |
| `IN_PROGRESS` | Em viagem |
| `COMPLETED` | Concluída |
| `CANCELED` | Cancelada |

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
├── fleet                   # FleetApi, BusInfo e DriverInfo (API pública)
│   └── internal
├── route                   # RouteApi (consulta da empresa de uma rota)
│   └── internal
├── trip                    # TripApi
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
4. **Relacionamentos entre módulos são feitos por UUID**, não por `@ManyToOne` (ex.: `User.companyUuid`, `Route.companyUuid`, `Trip.routeId`, `Ticket.routeId`).
5. **Usuário logado:** use `IamApi.currentUser()`, que devolve id, perfil e empresa. Nunca confie em empresa ou usuário enviados no corpo da requisição.
6. **Dados de outro módulo** são obtidos pela API pública dele, sem expor entidades internas. Ex.: a `trip` descobre a empresa de uma rota com `RouteApi.findCompanyUuid(routeId)`, que devolve só o UUID (ou 404 se a rota não existir).

`FleetApi.findBus(busId)` retorna `BusInfo(id, companyUuid, active, capacity)`, e `FleetApi.findDriver(driverId)` retorna `DriverInfo(id, companyUuid, active)`. Os records pertencem ao pacote público `fleet`. Essa API de consulta entre módulos não depende da role nem do contexto de autenticação do usuário. O caso de uso chamador deve autorizar a operação e comparar o `companyUuid` retornado com a empresa da rota, viagem ou outro recurso já autorizado antes de utilizar os dados. Os endpoints de gestão de fleet continuam restritos por role e tenant através dos serviços de aplicação e do `FleetAccessService`.

`findBus` e `findDriver` não retornam recursos excluídos nem recursos de empresa excluída; nesses casos, lançam `ResourceNotFoundException`. Recursos inativos retornam com `active = false`, e a capacidade considera apenas assentos ativos e não excluídos.

`FleetApi.seatBelongsToBus(seatId, busId)` verifica se o assento existe, pertence ao ônibus informado e não está excluído. Retorna `false` para assento inexistente, excluído ou de outro ônibus. A consulta reutiliza `SeatRepository.findByIdAndBus_IdAndIsDeletedFalse`, sem depender da role do usuário; verifica somente o vínculo e a exclusão do assento, sem validar estado ativo ou exclusão do ônibus/empresa.

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
- Regras de negócio e validações de estado ficam na entidade; os services só orquestram.

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
| Regra de negócio violada (ex.: código duplicado, chegada antes da partida) | `throw new BusinessException("...")` | 409 |
| Recurso não encontrado (ou excluído) | `throw new ResourceNotFoundException("...")` | 404 |
| Violação de restrição no banco (ex.: valor único repetido) | tratada automaticamente | 409 |
| Dados de entrada inválidos | `@Valid` no controller | 400 |
| Usuário sem permissão | `@PreAuthorize` ou `AccessDeniedException` | 403 |

### Permissões

- Declare o acesso em cada endpoint com `@PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")`. Use o nome do perfil sem o prefixo `ROLE_`.
- **Isolamento por empresa (multi-tenant):** é aplicado na consulta ao banco, e não depois de buscar. Os repositórios expõem `findVisibleById(id, companyUuid)` e `findAllVisible(companyUuid)`, que desconsideram excluídos e, quando `companyUuid` não é nulo, restringem à empresa. Assim, um registro de outra empresa simplesmente não é encontrado (404).
- **Qual empresa filtrar:** cada módulo tem um service de acesso (`FleetAccessService`, `RouteAccessService`, `TripAccessService`) que devolve `null` (sem restrição) ou a empresa do usuário logado. O FLEET_MANAGER sem empresa vinculada recebe 403.
- Outras restrições que dependem do dado (ex.: criar viagem em rota de outra empresa) ficam no service, lançando `AccessDeniedException`.

## Testes

```bash
./mvnw test
```

A infraestrutura do Docker precisa estar no ar.

| Teste | O que verifica |
|---|---|
| `ApiApplicationTests` | A aplicação inicia por completo |
| `ModularityTests` | As fronteiras entre módulos do Spring Modulith (`ApplicationModules.verify()`) |
| `IamApiTests` | A API pública do usuário logado |
| `FleetApiTests` | Contratos públicos de ônibus e motoristas, capacidade, estado ativo e recurso inexistente |
| `FleetApiIntegrationTests` | Consultas entre módulos independentes de role/autenticação, propriedade do recurso e filtros de exclusão |
| `UserSoftDeleteTests` | Exclusão lógica de usuários |
| `UpdateCompanyServiceTests` | Regras de atualização de empresas |
| `CompanyCrudIntegrationTests` | CRUD de empresas, acesso do gerente e ocultação da frota de empresas excluídas |
| `BusCrudIntegrationTests` / `DriverCrudIntegrationTests` | CRUDs com dois tenants, payload de empresa alheia, validações e exclusão lógica |
| `SeatCrudIntegrationTests` | Isolamento por ônibus, layout, capacidade e exclusão lógica de assentos |
| `FleetAuthorizationIntegrationTests` | Bloqueio dos demais perfis nos endpoints de fleet |
| `GlobalExceptionHandlerTests` | Mapeamento de exceções para códigos HTTP |

Os testes de integração de fleet usam a aplicação, segurança e consultas JPA reais, com dados revertidos ao fim de cada teste. Execute-os em um banco de testes separado, usando as variáveis `DB_*`; não use um banco com dados de produção.

### Integração contínua

O GitHub Actions (`.github/workflows/ci.yml`) roda `./mvnw test` em todo pull request e push para `develop` e `master`, com PostgreSQL, Redis e RabbitMQ como serviços. O resultado aparece como o check **CI / Testes** no pull request.

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
- **Códigos HTTP:**
  - Passagens ainda usam `BusinessException` para "não encontrado", retornando 409 em vez de 404.
  - Requisições sem token retornam 403 em vez de 401.
  - JSON malformado ou valor de enum inválido retornam 403 em vez de 400, porque a página de erro padrão (`/error`) é bloqueada pela segurança.
- **Viagens:** `busId` e `driverId` ainda não são validados pela API pública entre módulos (`FleetApi`), e as transições de situação ainda não são restritas (ex.: uma viagem cancelada pode voltar a `SCHEDULED`).
- **Empresa do ônibus:** `bus.company_id` é obrigatório. A implantação pressupõe frota inicialmente vazia; bancos com ônibus legados precisam de associação explícita à empresa antes de aplicar essa restrição.
- **Passagens:** ainda referenciam a rota (`routeId`); o próximo passo é referenciar a viagem (`tripId`).
- **Infraestrutura:** Redis e RabbitMQ já estão provisionados, mas ainda não são usados no código.
- **Módulo planejado:** `integration`.
