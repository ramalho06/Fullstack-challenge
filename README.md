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

## Sumário

- [Visão Geral](#visao-geral)
- [Tecnologias](#tecnologias)
- [Pré-requisitos](#pre-requisitos)
- [Como Rodar](#como-rodar)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Documentação](#documentacao)
- [Documentação da API](#documentacao-da-api)
- [Frontend](#frontend)
- [Fluxo recomendado de validação](#fluxo-recomendado-de-validacao)
- [Decisões Técnicas](#decisoes-tecnicas)
- [Limitações conhecidas](#limitacoes-conhecidas)
- [Estado dos Requisitos Importantes](#estado-dos-requisitos-importantes)
- [Diferenciais Implementados](#diferenciais-implementados)
- [Segurança](#seguranca)

<a id="visao-geral"></a>
## 📋 Visão Geral

O sistema permite:
- Gestão de agentes de campo (CRUD)
- Rastreamento geográfico em tempo real
- Integração com API GPS externa
- Sincronização automática via schedulers
- Monitoramento operacional
- Histórico de rotas e check-ins

<a id="tecnologias"></a>
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

### Frontend
| Tecnologia | Finalidade |
|---|---|
| Next.js 16 | Framework React (App Router) |
| Tailwind CSS | Estilização |
| TanStack Query | Estado servidor |
| React Hook Form + Zod | Formulários e validação |
| shadcn/ui | Componentes de UI |
| Leaflet + react-leaflet | Mapa, rotas e geofencing visual |

<a id="pre-requisitos"></a>
## ⚙️ Pré-requisitos

- **Java 17+** ([download](https://adoptium.net/))
- **Maven 3.8+** (ou usar o wrapper `./mvnw` incluído)
- **Node.js 20+** e **npm** para o frontend
- **Docker e Docker Compose** ([download](https://www.docker.com/products/docker-desktop/))
- **Git**

<a id="como-rodar"></a>
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

> O frontend ainda não está no Docker. Neste momento ele roda localmente com `npm run dev`.

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

### 7. Rodar o frontend localmente

Em outro terminal, com o backend em execução:

```bash
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

URL do frontend:

```txt
http://localhost:3000
```

Variável principal:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

O backend libera CORS para `http://localhost:3000` por padrão. Para alterar as origens permitidas:

```env
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000
```

O frontend já possui layout base, navegação, providers, shadcn/ui, TanStack Query, dashboard inicial na rota `/` e telas essenciais para agentes, check-ins, geofences e sincronização. As telas consomem APIs reais do backend.

### 8. Sincronizar agentes manualmente

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

### 9. Sincronizar localizações manualmente

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

### 10. Sincronizar check-ins manualmente

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

### 11. Sincronizar geofences manualmente

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
- O backend mantém `coordinatesJson` bruto e o frontend interpreta essas geometrias no mapa.

### 12. Schedulers automáticos

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

### 13. Endpoints de consulta e gestão

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

Geofences têm consulta paginada, mas CRUD fica fora deste passo. O `coordinatesJson` continua sendo entregue como texto bruto e é interpretado pelo frontend na camada visual do mapa.

### 14. Monitoramento operacional das sincronizações

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

<a id="estrutura-do-projeto"></a>
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
├── frontend/                   # Aplicação Next.js
│   ├── src/
│   │   ├── app/                # App Router
│   │   ├── components/         # Layout, providers e shadcn/ui
│   │   ├── features/           # Módulos de tela futuros
│   │   ├── services/           # API client e gateways HTTP
│   │   └── types/              # Tipos compartilhados da API
│   ├── .env.example
│   └── package.json
├── docs/                       # Documentação e decisões técnicas
│   ├── api-examples.md
│   └── decisions.md
├── docker-compose.yml          # Serviços de infraestrutura
├── .env.example                # Template de variáveis de ambiente
└── README.md
```

<a id="documentacao"></a>
## 📚 Documentação

- [Decisões Arquiteturais](docs/decisions.md) — Registro de decisões técnicas do projeto (ADRs).
- [Exemplos de uso da API](docs/api-examples.md) — Coleção de comandos `curl` para validar os endpoints principais.

<a id="documentacao-da-api"></a>
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

<a id="frontend"></a>
## 🖥️ Frontend

A rota `/` renderiza o dashboard operacional mínimo. Ela consome APIs reais do backend usando TanStack Query:

- `GET /api/v1/agents?page=0&size=100`
- `GET /api/v1/locations`
- `GET /api/v1/sync/status`
- `GET /api/v1/sync/executions/latest`

O dashboard mostra:

- total de agentes;
- agentes ativos;
- agentes online;
- última sincronização com sucesso;
- falhas de sincronização;
- tabela de últimas sincronizações;
- tabela de localizações atuais.

Os dados são atualizados periodicamente via `refetchInterval`. O dashboard é somente leitura e não concentra mapa, gráficos ou botões de sync manual; o mapa fica na rota `/map`.

As telas essenciais também já estão disponíveis:

- `/agents`: lista agentes, aplica filtros básicos, cria agente, edita agente e desativa agente via soft delete.
- `/check-ins`: lista check-ins, aplica filtros básicos e registra check-in manual selecionando agente real do backend.
- `/geofences`: lista geofences, filtra por tipo e exibe `coordinatesJson` completo em dialog.
- `/sync`: exibe status operacional, últimas execuções e histórico paginado de sincronizações.
- `/map`: exibe localizações atuais dos agentes em Leaflet, desenha a rota do dia com Polyline e renderiza geofences sincronizadas.

Formulários usam React Hook Form + Zod, mutations usam TanStack Query e ações exibem feedback com Sonner. O mapa consome `GET /api/v1/locations` com atualização a cada 30 segundos, a rota do dia consome `GET /api/v1/agents/{id}/route?date=YYYY-MM-DD` e a camada de geofences consome `GET /api/v1/geofences?page=0&size=100`.

As geofences são exibidas na rota `/map`: `CIRCLE` é renderizado como `Circle`, `POLYGON` é renderizado como `Polygon`, e o `coordinatesJson` recebido em `[longitude, latitude]` é convertido para o formato esperado pelo Leaflet, `[latitude, longitude]`. Geofences inválidas são ignoradas defensivamente para não quebrar o mapa. Ainda não há gráficos, botões de sync manual, rotas de detalhe, SSE/WebSocket ou alertas reais de entrada/saída em geofences.

<a id="fluxo-recomendado-de-validacao"></a>
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

<a id="decisoes-tecnicas"></a>
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

<a id="limitacoes-conhecidas"></a>
## ⚠️ Limitações conhecidas

- O frontend possui dashboard mínimo, telas essenciais e mapa com consumo real de APIs, mas rotas de detalhe e telas analíticas avançadas ficaram fora do escopo final.
- `SyncState` prepara a sincronização incremental por `syncToken`, mas a API externa testada não retornou um token funcional para check-ins.
- Circuit Breaker com Resilience4j não foi implementado; o retry atual é limitado e cobre `429` e `503`.
- WebSocket/SSE não foi implementado.
- Geofencing visual é apenas leitura: o mapa desenha áreas sincronizadas, mas não edita geofences nem executa alertas reais de entrada/saída.

<a id="estado-dos-requisitos-importantes"></a>
## ✅ Estado dos Requisitos Importantes

| Requisito | Status |
|---|---|
| Utilizar Next.js 16 com App Router | Implementado |
| Utilizar WebClient | Implementado |
| CRUD completo de agentes | Implementado |
| Atualização automática de posições | Implementado |
| Check-ins manuais | Implementado |
| Implementar os 4 schedulers obrigatórios | Implementado |
| Persistir histórico de sincronização | Implementado |
| Histórico completo de rota do dia | Implementado |
| Cálculo de distância (Haversine) | Implementado |
| Idempotência e conflitos de sincronização | Implementado |
| Integração externa com WebClient | Implementado |
| Tratamento de `429` e `503` | Implementado |
| Aplicar regras de negócio do documento | Implementado |
| Garantir tratamento adequado de erros e retries | Implementado |
| Monitoramento operacional da sincronização | Implementado |
| Dashboard frontend mínimo | Implementado |
| Telas essenciais do frontend | Implementado |
| Mapa com Leaflet | Implementado |
| Geofencing visual | Implementado |
| README e documentação técnica | Implementado |

<a id="diferenciais-implementados"></a>
## 🌟 Diferenciais Implementados

| Diferencial | Status |
|---|---|
| Testes automatizados | Implementado |
| Swagger/OpenAPI | Implementado |
| Dockerização do backend + MySQL | Implementado |
| Setup Next.js/Tailwind/shadcn/TanStack Query | Implementado |
| Dashboard frontend com dados reais | Implementado |
| CRUD visual de agentes e check-in manual no frontend | Implementado |
| Mapa interativo com Leaflet | Implementado |
| Geofencing visual | Implementado |
| Circuit Breaker com Resilience4j | Não iniciado |
| WebSocket/SSE | Não iniciado |

<a id="seguranca"></a>
## 🔐 Segurança

- Nenhuma credencial ou API Key é armazenada no código-fonte.
- Todas as configurações sensíveis são gerenciadas via variáveis de ambiente.
- O arquivo `.env` está incluído no `.gitignore` e **não deve ser commitado**.
