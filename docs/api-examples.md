# Exemplos de uso da API

Todos os exemplos assumem o backend rodando em `http://localhost:8080`.

## Health

```bash
curl "http://localhost:8080/api/health"
```

## Sincronizacao manual

```bash
curl -X POST "http://localhost:8080/api/v1/sync/agents"
```

```bash
curl -X POST "http://localhost:8080/api/v1/sync/locations"
```

```bash
curl -X POST "http://localhost:8080/api/v1/sync/check-ins"
```

```bash
curl -X POST "http://localhost:8080/api/v1/sync/geofences"
```

## Agentes

```bash
curl "http://localhost:8080/api/v1/agents?page=0&size=20"
```

```bash
curl "http://localhost:8080/api/v1/agents?search=Alpha&page=0&size=20"
```

```bash
curl "http://localhost:8080/api/v1/agents?active=true&status=ONLINE&page=0&size=20"
```

```bash
curl "http://localhost:8080/api/v1/agents/seed_agent_001"
```

```bash
curl -X POST "http://localhost:8080/api/v1/agents" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Agente Local",
    "role": "TECHNICIAN",
    "team": "Alpha",
    "phone": "+5511999999999",
    "email": "agente.local@example.com",
    "active": true,
    "status": "OFFLINE"
  }'
```

```bash
curl -X PUT "http://localhost:8080/api/v1/agents/local_agent_exemplo" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Agente Atualizado",
    "role": "INSTALLER",
    "team": "Beta",
    "phone": "+5511888888888",
    "email": "agente.atualizado@example.com",
    "active": true,
    "status": "ONLINE"
  }'
```

```bash
curl -X DELETE "http://localhost:8080/api/v1/agents/local_agent_exemplo"
```

## Localizacoes

```bash
curl "http://localhost:8080/api/v1/locations"
```

```bash
curl "http://localhost:8080/api/v1/agents/seed_agent_001/location"
```

## Check-ins

```bash
curl "http://localhost:8080/api/v1/check-ins?page=0&size=20"
```

```bash
curl -X POST "http://localhost:8080/api/v1/check-ins" \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "seed_agent_001",
    "type": "CHECKIN",
    "latitude": -23.5505,
    "longitude": -46.6333,
    "address": "Av. Paulista, 1000 - Sao Paulo, SP",
    "accuracy": 10,
    "speed": 0,
    "notes": "Check-in manual"
  }'
```

## Geofences

```bash
curl "http://localhost:8080/api/v1/geofences?page=0&size=20"
```

```bash
curl "http://localhost:8080/api/v1/geofences?page=0&size=100"
```

## Rota do dia

```bash
curl "http://localhost:8080/api/v1/agents/seed_agent_001/route?date=2026-05-22"
```

## Monitoramento de sincronizacao

```bash
curl "http://localhost:8080/api/v1/sync/status"
```

```bash
curl "http://localhost:8080/api/v1/sync/executions?page=0&size=20&sort=startedAt,desc"
```

```bash
curl "http://localhost:8080/api/v1/sync/executions/latest"
```
