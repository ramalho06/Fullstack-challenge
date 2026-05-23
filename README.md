# 🛰️ Teams Tracking System

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

### 3. Subir o MySQL com Docker

```bash
docker compose up -d
```

Verifique se o MySQL está saudável:

```bash
docker compose ps
```

### 4. Rodar o Backend

```bash
cd backend

# Exportar variáveis de ambiente (Linux/Mac)
export $(cat ../.env | grep -v '^#' | xargs)

# Compilar e rodar
./mvnw spring-boot:run
```

**Alternativa (IntelliJ IDEA):** Configure as variáveis de ambiente na Run Configuration do IntelliJ.

### 5. Verificar se está funcionando

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

### 6. Sincronizar agentes manualmente

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

> Os schedulers ainda não foram implementados. Por enquanto, a sincronização é manual para validar o caso de uso antes da automação.

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
│   └── decisions.md
├── docker-compose.yml          # Serviços de infraestrutura
├── .env.example                # Template de variáveis de ambiente
└── README.md
```

## 📚 Documentação

- [Decisões Arquiteturais](docs/decisions.md) — Registro de decisões técnicas do projeto (ADRs).

## 🧭 Decisões Técnicas

- O backend foi iniciado antes do frontend para reduzir cedo o risco de integração, persistência e sincronização.
- O schema do banco é controlado por Flyway, com Hibernate em modo `validate`.
- `Agent`, `CheckIn` e `Geofence` usam IDs textuais porque a API externa retorna identificadores canônicos como `seed_agent_002`.
- `Agent` guarda a localização atual e `LocationHistory` guarda o histórico de pontos válidos para rotas.
- Timestamps de domínio usam `Instant`, pois a API retorna datas em UTC com sufixo `Z`.
- A sincronização manual de agentes veio antes dos schedulers para validar o caso de uso de ponta a ponta.
- O scheduler futuro deverá chamar o mesmo `AgentSyncService` usado pelo endpoint manual.
- `SyncState` é usado na sincronização de check-ins para preparar incrementalidade sem inventar tokens locais.
- O acesso à API externa fica isolado atrás de gateways/clients em `external/`, sem misturar DTO externo com entidade JPA.
- Retries de API externa são limitados e preparados para `429` com `Retry-After` e `503` com backoff exponencial e jitter.

## ✅ Estado dos Requisitos Importantes

| Requisito | Status |
|---|---|
| Utilizar Next.js 16 com App Router | Não iniciado |
| Utilizar WebClient | Implementado |
| Implementar os 4 schedulers obrigatórios | Não iniciado |
| Persistir histórico de sincronização | Parcial: `SyncExecution` registra sync de agentes, localizações e check-ins |
| Aplicar regras de negócio do documento | Parcial: upsert de agentes, idempotência, descarte de GPS impreciso e sync de check-ins |
| Garantir tratamento adequado de erros e retries | Parcial: implementado nos clients de agentes, localizações e check-ins |
| Documentar decisões técnicas no README | Implementado com resumo e link para ADRs |

## 🔐 Segurança

- Nenhuma credencial ou API Key é armazenada no código-fonte.
- Todas as configurações sensíveis são gerenciadas via variáveis de ambiente.
- O arquivo `.env` está incluído no `.gitignore` e **não deve ser commitado**.
