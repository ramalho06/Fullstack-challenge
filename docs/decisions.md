# ADR - Architecture Decision Records

Registro de decisões arquiteturais do projeto **Teams Tracking System**.

Cada decisão é registrada com contexto, alternativas consideradas e justificativa.
Formato inspirado no [ADR (Architecture Decision Records)](https://adr.github.io/).

---

## ADR-001: Começar pelo backend e pela fundação de sincronização antes do frontend

**Data:** 2025-05-22

**Status:** Aceita

### Contexto

O desafio exige um sistema com integração a API externa, sincronização de dados via schedulers, regras de negócio de geolocalização, persistência de histórico e resiliência a falhas (rate limiting, instabilidade). O frontend consome esses dados já processados.

### Alternativas Consideradas

1. **Começar pelo frontend e mockar a API**
   - *Prós:* resultados visuais rápidos; motiva o desenvolvimento.
   - *Contras:* sem API real, o mock pode divergir do contrato real; risco de retrabalho quando o backend ficar pronto; não ataca o risco técnico principal.

2. **Desenvolver frontend e backend simultaneamente**
   - *Prós:* progresso paralelo em ambas as frentes.
   - *Contras:* exige definir contratos de API antecipadamente; mudanças no backend forçam refatoração no frontend; mais complexidade de coordenação para um time de uma pessoa.

3. **Começar pelo backend (escolhida)**
   - *Prós:* ataca o risco técnico mais alto primeiro (integração, sincronização, resiliência); o frontend depende de APIs bem modeladas — tê-las prontas evita retrabalho; permite validar regras de negócio isoladamente com testes.
   - *Contras:* sem resultado visual nos primeiros dias.

### Decisão

Iniciar pelo backend com foco na fundação: configuração do projeto, conexão com banco de dados, integração com a API externa e mecanismo de sincronização.

### Justificativa

- A parte mais crítica e complexa do desafio é a **integração com a API externa**, incluindo tratamento de rate limiting (429), instabilidade (503), paginação, sincronização incremental via `syncToken` e idempotência.
- O frontend depende de endpoints bem definidos e dados consistentes no banco. Construí-lo antes significaria trabalhar com contratos instáveis.
- Começar pelo backend permite **reduzir o risco técnico cedo**, garantindo que a fundação esteja sólida antes de investir tempo na camada visual.
- Essa abordagem é alinhada com a prática de **"Walking Skeleton"** — construir a infraestrutura mínima funcional ponta a ponta antes de adicionar funcionalidades.

### Consequências

- O progresso visual (telas, mapas) só aparecerá nos dias finais.
- O backend precisa ter endpoints testáveis o mais cedo possível para desbloquear o frontend.
- Commits iniciais serão focados em infraestrutura e configuração, não em features visíveis.

---

## Decisão 002 — Modelagem inicial alinhada à API de rastreamento

**Data:** 2026-05-23

**Status:** Aceita

### Contexto

A API externa e o contrato OpenAPI do desafio descrevem agentes, localizações atuais, histórico de rota, check-ins, geofences e sincronizações incrementais. Antes de criar controllers, services ou schedulers, o modelo de domínio precisa refletir esses conceitos para evitar retrabalho nas próximas etapas.

### Decisão

Criar as entidades iniciais `Agent`, `LocationHistory`, `CheckIn`, `Geofence`, `SyncExecution` e `SyncState`, com migrations Flyway como fonte versionada do schema do banco. O Hibernate passa a usar `ddl-auto=validate`, validando se as entidades continuam compatíveis com as tabelas criadas pelas migrations.

### Justificativa

- `Agent` guarda o cadastro do agente e também o estado operacional atual, incluindo localização atual, status, bateria e último sinal.
- `LocationHistory` guarda o histórico consolidado de pontos válidos, que será usado futuramente para rotas do dia.
- `CheckIn` representa eventos manuais e eventos sincronizados da API externa, como parada detectada, sinal perdido e entrada ou saída de geofence.
- `Geofence` foi incluída desde o início porque a sincronização de geofences é obrigatória no desafio.
- `SyncExecution` e `SyncState` foram separados para diferenciar auditoria histórica de estado operacional atual.
- `SyncExecution` registra cada execução de sincronização, seus contadores, erros e os tokens antes/depois da execução.
- `SyncState` guarda o `lastSyncToken` e os horários usados pela próxima sincronização incremental.
- `externalId` e `externalEventId` são essenciais para upsert e idempotência, evitando registros duplicados quando uma sincronização for repetida.
- Os campos de localização atual no `Agent` evitam consultas pesadas ao histórico para telas de dashboard e listagem de localizações atuais.

### Consequências

- A evolução do banco passa a ser controlada por migrations, aumentando previsibilidade e facilitando revisão.
- Mudanças futuras em entidades precisarão vir acompanhadas de novas migrations.
- Services de sincronização poderão ser implementados depois usando `externalId`, `externalEventId`, `SyncExecution` e `SyncState` como base para resiliência e idempotência.

---

## Decisão 003 — IDs textuais alinhados ao contrato da API

**Data:** 2026-05-23

**Status:** Aceita

### Contexto

Os exemplos reais da API externa mostram que alguns recursos possuem IDs canônicos textuais. Agentes usam valores como `seed_agent_002`, check-ins usam valores como `seed_ci_010` e geofences usam valores como `seed_geo_003`.

Modelar esses recursos com `Long` e `@GeneratedValue` criaria um identificador interno que não existe no contrato da API, dificultando sincronização, comparação de dados, upsert e troubleshooting.

### Decisão

`Agent`, `CheckIn` e `Geofence` passam a usar `String` como chave primária. A classe `BaseEntity` deixa de definir `id` e passa a ser apenas uma base de auditoria com `createdAt` e `updatedAt`.

`LocationHistory`, `SyncExecution` e `SyncState` continuam usando `Long` com auto incremento, porque são tabelas internas do sistema e não representam recursos canônicos retornados pela API externa.

Também foi adotado `Instant` para campos temporais, porque a API retorna timestamps em UTC com sufixo `Z`.

### Justificativa

- `Agent.id` preserva o identificador canônico da API, como `seed_agent_002`.
- `CheckIn.id` preserva o identificador canônico do check-in, como `seed_ci_010`.
- `Geofence.id` preserva o identificador canônico da geofence, como `seed_geo_003`.
- `Agent.externalId` e `Geofence.externalId` continuam existindo porque são campos distintos no contrato e serão úteis para upsert.
- `CheckIn.externalEventId` continua existindo porque será usado para idempotência de eventos sincronizados.
- `LocationHistory` continua com `Long` porque a API de localizações atuais não fornece ID próprio de localização; essa tabela é uma consolidação interna para histórico de rotas.
- `SyncExecution` e `SyncState` continuam com `Long` porque representam auditoria e estado operacional internos.
- `BaseEntity` não deve impor uma estratégia de ID, já que cada entidade pode ter uma identidade diferente.
- `Instant` evita perda de informação de timezone e representa corretamente um ponto exato no tempo.

### Consequências

- Relacionamentos com `Agent` passam a usar `agent_id` textual (`VARCHAR(80)`).
- Repositories de `Agent`, `CheckIn` e `Geofence` passam a usar `String` como tipo de ID.
- Check-ins manuais futuros precisarão gerar IDs textuais locais, por exemplo `local_ci_<uuid>`.
- Migrations futuras devem respeitar a separação entre IDs canônicos externos e IDs internos do sistema.
