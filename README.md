# 🛰️ Teams Tracking System

![Java 17+](https://img.shields.io/badge/Java-17%2B-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.4.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-Data-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![WebClient](https://img.shields.io/badge/WebClient-Reactive-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![MySQL 8](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-Migrations-CC0200?style=for-the-badge&logo=flyway&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Swagger OpenAPI](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![Next.js 16](https://img.shields.io/badge/Next.js-16-000000?style=for-the-badge&logo=nextdotjs&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-Frontend-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white)
![TanStack Query](https://img.shields.io/badge/TanStack%20Query-Frontend-FF4154?style=for-the-badge&logo=reactquery&logoColor=white)
![React Hook Form](https://img.shields.io/badge/React%20Hook%20Form-Forms-EC5990?style=for-the-badge&logo=reacthookform&logoColor=white)
![Zod](https://img.shields.io/badge/Zod-Validation-3E67B1?style=for-the-badge)
![shadcn/ui](https://img.shields.io/badge/shadcn%2Fui-Components-000000?style=for-the-badge&logo=shadcnui&logoColor=white)

Sistema de rastreamento de equipes externas em tempo real, com integração a API GPS, sincronização automática de dados e monitoramento operacional.

## 📋 Visão Geral

O sistema permite:
- Gestão de agentes de campo (CRUD)
- Rastreamento geográfico em tempo real
- Integração com API GPS externa
- Sincronização automática via schedulers
- Monitoramento operacional
- Histórico de rotas e check-ins

## 🛠️ Tecnologias

### Backend
| Tecnologia | Versão | Finalidade |
|---|---|---|
| Java | 17+ | Linguagem principal |
| Spring Boot | 3.4.x | Framework web |
| Spring Data JPA | - | Acesso a dados |
| Flyway | - | Versionamento do banco |
| Spring WebFlux | - | WebClient (HTTP reativo) |
| MySQL | 8.0 | Banco de dados relacional |

### Frontend *(em desenvolvimento)*
| Tecnologia | Finalidade |
|---|---|
| Next.js 16 | Framework React (App Router) |
| Tailwind CSS | Estilização |
| TanStack Query | Estado servidor |
| React Hook Form + Zod | Formulários e validação |
| shadcn/ui | Componentes de UI |

## ⚙️ Pré-requisitos

- **Java 17+** ([download](https://adoptium.net/))
- **Maven 3.8+** (ou usar o wrapper `./mvnw` incluído)
- **Docker e Docker Compose** ([download](https://www.docker.com/products/docker-desktop/))
- **Git**

## 🚀 Como Rodar

### 1. Clonar o repositório

```bash
git clone <url-do-repositorio>
cd teams-tracking-system
```

### 2. Configurar variáveis de ambiente

Copie o arquivo de exemplo e preencha com seus valores:

```bash
cp .env.example .env
```

Edite o arquivo `.env` e configure:
- Credenciais do MySQL
- URL de conexão do banco
- URL base da API externa em `EXTERNAL_API_BASE_URL`
- API Key da API externa em `EXTERNAL_API_KEY`

> ⚠️ **IMPORTANTE:** A API Key real **nunca** deve ser commitada no repositório. O arquivo `.env` está no `.gitignore`.

### 3. Executar backend + MySQL com Docker

Com o `.env` configurado, suba a aplicação completa:

```bash
docker compose up --build
```

Esse comando sobe:
- `mysql`
- `backend`

URLs principais:
- Backend: http://localhost:8080
- Health check: http://localhost:8080/api/health
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- MySQL: `localhost:3306`

O Flyway roda automaticamente quando o backend sobe. Dentro do Docker, o backend acessa o banco pelo hostname `mysql`, não por `localhost`.

Para parar os containers:

```bash
docker compose down
```

Para parar e remover o volume do banco, resetando os dados:

```bash
docker compose down -v
```

> O frontend ainda não está no Docker porque será implementado em um passo futuro.

### 4. Alternativa: subir apenas o MySQL com Docker e rodar o backend localmente

```bash
docker compose up -d mysql
```

Verifique se o MySQL está saudável:

```bash
docker compose ps
```

### 5. Rodar o Backend localmente

```bash
cd backend

# Exportar variáveis de ambiente (Linux/Mac)
export $(cat ../.env | grep -v '^#' | xargs)

# Compilar e rodar
./mvnw spring-boot:run
```

**Alternativa (IntelliJ IDEA):** Configure as variáveis de ambiente na Run Configuration do IntelliJ.

### 6. Verificar se está funcionando

```bash
curl http://localhost:8080/api/health
```

Resposta esperada:
```json
{
  "status": "UP",
  "timestamp": "2025-05-22T22:00:00.000",
  "service": "Teams Tracking API"
}
```

### 7. Sincronizar agentes manualmente

Com o backend rodando e as variáveis `EXTERNAL_API_BASE_URL` e `EXTERNAL_API_KEY` configuradas no `.env`, execute:

```bash
curl -X POST http://localhost:8080/api/v1/sync/agents
```

Resposta esperada:
```json
{
  "syncType": "AGENTS",
  "status": "SUCCESS",
  "processed": 5,
  "created": 5,
  "updated": 0,
  "skipped": 0,
  "startedAt": "2026-05-23T14:00:00Z",
  "finishedAt": "2026-05-23T14:00:01Z"
}
```

> Os schedulers automáticos também chamam o mesmo service. O endpoint manual permanece útil para testes e reprocessamentos controlados.

### 7. Sincronizar localizações manualmente

Execute primeiro a sincronização de agentes, pois localizações de agentes inexistentes são ignoradas para evitar cadastro parcial:

```bash
curl -X POST http://localhost:8080/api/v1/sync/agents
curl -X POST http://localhost:8080/api/v1/sync/locations
```

Resposta esperada:
```json
{
  "syncType": "LOCATIONS",
  "status": "SUCCESS",
  "processed": 4,
  "created": 4,
  "updated": 4,
  "skipped": 0,
  "startedAt": "2026-05-23T14:05:00Z",
  "finishedAt": "2026-05-23T14:05:01Z"
}
```

Regras aplicadas:
- `accuracy > 50` descarta a leitura completamente.
- `accuracy = null` é aceita.
- `latitude`, `longitude`, `lastSeen` ou `agentId` ausentes geram `skipped`.
- A idempotência do histórico usa `agent_id + recorded_at + source`, evitando duplicação ao rodar o endpoint mais de uma vez.

### 8. Sincronizar check-ins manualmente

Execute primeiro a sincronização de agentes, pois check-ins de agentes inexistentes são ignorados:

```bash
curl -X POST http://localhost:8080/api/v1/sync/agents
curl -X POST http://localhost:8080/api/v1/sync/check-ins
```

Resposta esperada:
```json
{
  "syncType": "CHECK_INS",
  "status": "SUCCESS",
  "processed": 10,
  "created": 10,
  "updated": 0,
  "skipped": 0,
  "startedAt": "2026-05-23T14:10:00Z",
  "finishedAt": "2026-05-23T14:10:01Z"
}
```

Regras aplicadas:
- O backend consome `GET /api/v1/check-ins` da API externa.
- O `POST /api/v1/sync/check-ins` externo não é usado para buscar eventos.
- A idempotência usa `CheckIn.id` como PK e `externalEventId` como unique adicional.
- `SyncState` está preparado para token incremental, mas a API atual não retorna `syncToken` funcional.
- Check-ins com `accuracy > 50` são salvos, mas não geram `LocationHistory`.
- Check-ins com coordenadas e `accuracy <= 50` ou `accuracy = null` podem gerar `LocationHistory`.

### 9. Sincronizar geofences manualmente

```bash
curl -X POST http://localhost:8080/api/v1/sync/geofences
```

Resposta esperada:
```json
{
  "syncType": "GEOFENCES",
  "status": "SUCCESS",
  "processed": 3,
  "created": 3,
  "updated": 0,
  "skipped": 0,
  "startedAt": "2026-05-23T14:15:00Z",
  "finishedAt": "2026-05-23T14:15:01Z"
}
```

Regras aplicadas:
- O backend consome `GET /api/v1/geofences` da API externa.
- O upsert é feito por `externalId`, preservando o `id` textual retornado pela API.
- `coordinatesJson` é salvo bruto como `String`/`TEXT`, sem parse ou normalização geométrica.
- `assignedTeams` é salvo como `String`, sem tabela de equipes.
- Geofencing visual e mapa interativo continuam como diferenciais futuros.

### 10. Schedulers automáticos

Os quatro schedulers obrigatórios usam os mesmos services dos endpoints manuais:

| Scheduler | Frequência | Initial delay |
|---|---:|---:|
| Agents | 10 minutos | 30 segundos |
| Locations | 1 minuto | 45 segundos |
| Check-ins | 2 minutos | 60 segundos |
| Geofences | 30 minutos | 90 segundos |

As frequências são configuráveis em `application.yml`:

```yaml
app:
  schedulers:
    enabled: true
    agents-fixed-delay-ms: 600000
    agents-initial-delay-ms: 30000
    locations-fixed-delay-ms: 60000
    locations-initial-delay-ms: 45000
    check-ins-fixed-delay-ms: 120000
    check-ins-initial-delay-ms: 60000
    geofences-fixed-delay-ms: 1800000
    geofences-initial-delay-ms: 90000
```

`app.schedulers.enabled=false` desabilita os gatilhos automáticos. Cada rotina usa um `AtomicBoolean` próprio para impedir sobreposição local da mesma sincronização.

Também é possível desligar os schedulers por variável de ambiente:

```bash
APP_SCHEDULERS_ENABLED=false
```

Ou ao rodar localmente:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--app.schedulers.enabled=false"
```

### 11. Endpoints de consulta e gestão

O backend expõe DTOs para o frontend e não retorna entidades JPA diretamente. Endpoints paginados usam um DTO próprio com `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last` e `empty`.

#### Agentes

```bash
curl "http://localhost:8080/api/v1/agents?page=0&size=20"
curl "http://localhost:8080/api/v1/agents?active=true&status=ONLINE&page=0&size=20"
curl http://localhost:8080/api/v1/agents/seed_agent_001
```

Filtros suportados: `active`, `status`, `role`, `team`, `search`, `page`, `size` e `sort`.

Criar agente local:

```bash
curl -X POST http://localhost:8080/api/v1/agents \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Agente Local",
    "role": "TECHNICIAN",
    "team": "Alpha",
    "phone": "+55 11 99999-0000",
    "email": "agente.local@example.com",
    "active": true,
    "status": "OFFLINE"
  }'
```

Agentes criados localmente recebem `id` no formato `local_agent_<uuid>` e `externalId` no formato `local-ext-agent_<uuid>`. O frontend não precisa enviar esses campos técnicos.

Atualizar dados cadastrais:

```bash
curl -X PUT http://localhost:8080/api/v1/agents/<agent-id> \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Agente Local Atualizado",
    "role": "MAINTENANCE",
    "team": "Beta",
    "phone": "+55 11 98888-0000",
    "email": "agente.atualizado@example.com",
    "active": true,
    "status": "PAUSED"
  }'
```

Remover agente:

```bash
curl -X DELETE http://localhost:8080/api/v1/agents/<agent-id>
```

O delete é lógico: o registro permanece no banco, com `active=false` e `status=OFFLINE`. Isso preserva histórico e evita quebrar relacionamentos.

#### Localizações

```bash
curl "http://localhost:8080/api/v1/locations?active=true"
curl "http://localhost:8080/api/v1/locations?onlineOnly=true"
curl http://localhost:8080/api/v1/agents/seed_agent_001/location
```

`GET /api/v1/agents/{id}/location` retorna o agente mesmo quando ainda não há coordenadas conhecidas; nesse caso, os campos de localização vêm como `null`.

#### Check-ins

```bash
curl "http://localhost:8080/api/v1/check-ins?page=0&size=20"
curl "http://localhost:8080/api/v1/check-ins?agentId=seed_agent_001&type=CHECKIN&page=0&size=20"
```

Filtros suportados: `agentId`, `type`, `source`, `page`, `size` e `sort`.

Registrar check-in manual:

```bash
curl -X POST http://localhost:8080/api/v1/check-ins \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "seed_agent_001",
    "type": "CHECKIN",
    "latitude": -23.5505200,
    "longitude": -46.6333080,
    "address": "Praça da Sé - São Paulo, SP",
    "accuracy": 12.5,
    "speed": 0,
    "notes": "Check-in manual"
  }'
```

Check-ins manuais recebem `id` no formato `local_ci_<uuid>`, `source=MANUAL` e `externalEventId=null`. Quando há coordenadas e a acurácia é aceitável, o backend também cria um ponto em `LocationHistory` com `source=MANUAL_CHECKIN`.

#### Rota do dia

```bash
curl "http://localhost:8080/api/v1/agents/seed_agent_001/route?date=2026-05-22"
```

Regras aplicadas:
- `date` é interpretado no timezone `America/Sao_Paulo`.
- A rota usa apenas `LocationHistory`, fonte consolidada dos pontos geográficos.
- Pontos com `accuracy > 50` são ignorados defensivamente.
- Pontos com `accuracy = null` são aceitos.
- `GPS_SYNC`, `MANUAL_CHECKIN` e `EVENT_SYNC` entram na rota.
- `totalDistanceMeters` e `distanceFromPreviousMeters` são calculados com Haversine.
- Se o agente existir sem pontos no dia, a resposta é `200 OK` com `points=[]` e `totalDistanceMeters=0.00`.
- `404` é retornado apenas quando o agente não existe.

#### Geofences

```bash
curl "http://localhost:8080/api/v1/geofences?page=0&size=20"
curl "http://localhost:8080/api/v1/geofences?type=CIRCLE&page=0&size=20"
```

Filtros suportados: `type`, `page`, `size` e `sort`.

Geofences têm consulta paginada, mas CRUD fica fora deste passo. O `coordinatesJson` continua sendo entregue como texto bruto para o frontend interpretar futuramente no mapa.

### 12. Monitoramento operacional das sincronizações

O backend expõe endpoints de leitura para acompanhar histórico, últimas execuções e configuração dos schedulers.

Listar execuções com paginação:

```bash
curl "http://localhost:8080/api/v1/sync/executions?page=0&size=20&sort=startedAt,desc"
```

Filtrar por tipo e status:

```bash
curl "http://localhost:8080/api/v1/sync/executions?syncType=AGENTS&status=FAILED&page=0&size=20"
```

Buscar a última execução registrada de cada tipo:

```bash
curl "http://localhost:8080/api/v1/sync/executions/latest"
```

Consultar o status operacional consolidado:

```bash
curl "http://localhost:8080/api/v1/sync/status"
```

Regras aplicadas:
- `overallStatus=HEALTHY` quando não há falhas ou sucessos parciais nas últimas execuções.
- `overallStatus=WARNING` quando alguma última execução está como `PARTIAL_SUCCESS`.
- `overallStatus=DEGRADED` quando alguma última execução está como `FAILED`.
- `FAILED` tem prioridade sobre `PARTIAL_SUCCESS`.
- A resposta inclui `fixedDelayMs` e `initialDelayMs` lidos de `SchedulerProperties`.
- `errorMessage` é resumido e não expõe stacktrace.

## 📁 Estrutura do Projeto

```
teams-tracking-system/
├── backend/                    # API Spring Boot
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/media4all/tracking/
│   │   │   │   ├── agent/      # Domínio e sincronização de agentes
│   │   │   │   ├── checkin/    # Modelo de check-ins
│   │   │   │   ├── common/     # Base comum e tratamento de erro
│   │   │   │   ├── config/     # Configurações (WebClient, etc.)
│   │   │   │   ├── external/   # Clients e DTOs da API externa
│   │   │   │   ├── geofence/   # Modelo de geofences
│   │   │   │   ├── health/     # Health check
│   │   │   │   ├── location/   # Histórico de localizações
│   │   │   │   └── sync/       # Auditoria e endpoints de sincronização
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/
│   │   └── test/
│   └── pom.xml
├── frontend/                   # Next.js (em desenvolvimento)
├── docs/                       # Documentação e decisões técnicas
│   ├── api-examples.md
│   └── decisions.md
├── docker-compose.yml          # Serviços de infraestrutura
├── .env.example                # Template de variáveis de ambiente
└── README.md
```

## 📚 Documentação

- [Decisões Arquiteturais](docs/decisions.md) — Registro de decisões técnicas do projeto (ADRs).
- [Exemplos de uso da API](docs/api-examples.md) — Coleção de comandos `curl` para validar os endpoints principais.

## 📖 Documentação da API

A documentação OpenAPI é gerada via `springdoc-openapi` e fica disponível com o backend em execução:

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Os endpoints estão agrupados por domínio:
- Health
- Agents
- Locations
- Check-ins
- Geofences
- Routes
- Sync commands
- Sync monitoring

O Swagger documenta DTOs públicos, exemplos dos principais endpoints e o contrato padronizado de erro (`ApiErrorResponse`). Ele não expõe `EXTERNAL_API_KEY`, variáveis de ambiente sensíveis ou headers internos. A API ainda não possui autenticação neste desafio, por isso nenhum `securityScheme` foi configurado.

## 🧪 Fluxo recomendado de validação

Para validar o backend do zero:

```bash
docker compose down -v
docker compose up --build
```

Depois, em outro terminal:

```bash
curl "http://localhost:8080/api/health"
curl -X POST "http://localhost:8080/api/v1/sync/agents"
curl -X POST "http://localhost:8080/api/v1/sync/locations"
curl -X POST "http://localhost:8080/api/v1/sync/check-ins"
curl -X POST "http://localhost:8080/api/v1/sync/geofences"
curl "http://localhost:8080/api/v1/agents?page=0&size=20"
curl "http://localhost:8080/api/v1/locations"
curl "http://localhost:8080/api/v1/sync/status"
```

Com esse fluxo, o MySQL sobe limpo, o Flyway valida/aplica as migrations e os principais casos de uso ficam verificáveis.

## 🧭 Decisões Técnicas

- O backend foi iniciado antes do frontend para reduzir cedo o risco de integração, persistência e sincronização.
- O schema do banco é controlado por Flyway, com Hibernate em modo `validate`.
- `Agent`, `CheckIn` e `Geofence` usam IDs textuais porque a API externa retorna identificadores canônicos como `seed_agent_002`.
- `Agent` guarda a localização atual e `LocationHistory` guarda o histórico de pontos válidos para rotas.
- Timestamps de domínio usam `Instant`, pois a API retorna datas em UTC com sufixo `Z`.
- A sincronização manual de agentes veio antes dos schedulers para validar o caso de uso de ponta a ponta.
- Os schedulers automáticos reutilizam os mesmos services dos endpoints manuais, sem duplicar regra de negócio.
- Cada scheduler usa `AtomicBoolean` para impedir sobreposição local da mesma rotina.
- `SyncState` é usado na sincronização de check-ins para preparar incrementalidade sem inventar tokens locais.
- Geofences são sincronizadas por `externalId`, mantendo `coordinatesJson` bruto para evitar complexidade espacial prematura.
- O acesso à API externa fica isolado atrás de gateways/clients em `external/`, sem misturar DTO externo com entidade JPA.
- Retries de API externa são limitados e preparados para `429` com `Retry-After` e `503` com backoff exponencial e jitter.

## ⚠️ Limitações conhecidas

- O frontend ainda não foi implementado; os badges e a estrutura indicam a stack planejada.
- `SyncState` prepara a sincronização incremental por `syncToken`, mas a API externa testada não retornou um token funcional para check-ins.
- Circuit Breaker com Resilience4j não foi implementado; o retry atual é limitado e cobre `429` e `503`.
- WebSocket/SSE não foi implementado.
- Leaflet, mapa interativo e geofencing visual ainda são diferenciais futuros.

## ✅ Estado dos Requisitos Importantes

| Requisito | Status |
|---|---|
| Utilizar Next.js 16 com App Router | Não iniciado |
| Utilizar WebClient | Implementado |
| Implementar os 4 schedulers obrigatórios | Implementado |
| Persistir histórico de sincronização | Parcial: `SyncExecution` registra sync de agentes, localizações, check-ins e geofences |
| Histórico completo de rota do dia | Implementado |
| Cálculo de distância (Haversine) | Implementado |
| Aplicar regras de negócio do documento | Parcial: upsert de agentes/geofences, idempotência, descarte de GPS impreciso, sync de check-ins, CRUD de agentes, check-in manual e rota do dia |
| Garantir tratamento adequado de erros e retries | Parcial: implementado nos clients de agentes, localizações, check-ins e geofences |
| Monitoramento operacional da sincronização | Implementado |
| Documentar decisões técnicas no README | Implementado com resumo e link para ADRs |

## 🌟 Diferenciais Implementados

| Diferencial | Status |
|---|---|
| Testes automatizados | Implementado |
| Swagger/OpenAPI | Implementado |
| Dockerização do backend + MySQL | Implementado |
| Circuit Breaker com Resilience4j | Não iniciado |
| WebSocket/SSE | Não iniciado |
| Mapa interativo com Leaflet | Não iniciado |
| Geofencing visual | Não iniciado |

## 🔐 Segurança

- Nenhuma credencial ou API Key é armazenada no código-fonte.
- Todas as configurações sensíveis são gerenciadas via variáveis de ambiente.
- O arquivo `.env` está incluído no `.gitignore` e **não deve ser commitado**.
