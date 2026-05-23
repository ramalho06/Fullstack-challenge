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
| Spring WebFlux | - | WebClient (HTTP reativo) |
| MySQL | 8.0 | Banco de dados relacional |
| Lombok | - | Redução de boilerplate |

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

## 📁 Estrutura do Projeto

```
teams-tracking-system/
├── backend/                    # API Spring Boot
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/media4all/tracking/
│   │   │   │   ├── config/     # Configurações (WebClient, etc.)
│   │   │   │   ├── controller/ # Endpoints REST
│   │   │   │   ├── service/    # Regras de negócio
│   │   │   │   ├── repository/ # Acesso a dados (JPA)
│   │   │   │   ├── model/      # Entidades JPA
│   │   │   │   ├── dto/        # Data Transfer Objects
│   │   │   │   └── scheduler/  # Jobs de sincronização
│   │   │   └── resources/
│   │   │       └── application.yml
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

## 🔐 Segurança

- Nenhuma credencial ou API Key é armazenada no código-fonte.
- Todas as configurações sensíveis são gerenciadas via variáveis de ambiente.
- O arquivo `.env` está incluído no `.gitignore` e **não deve ser commitado**.
