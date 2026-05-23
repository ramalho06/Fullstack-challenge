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

---

## Decisão 004 — Sincronização manual de agentes antes dos schedulers

**Data:** 2026-05-23

**Status:** Aceita

### Contexto

O desafio exige sincronização automática via schedulers, mas a integração com a API externa envolve paginação, rate limiting, instabilidade simulada, upsert e auditoria. Antes de automatizar esse fluxo, é melhor validar o caso de uso manualmente com um endpoint explícito.

### Decisão

Implementar primeiro a sincronização manual de agentes em `POST /api/v1/sync/agents`. O endpoint chama `AgentSyncService`, que concentra o caso de uso. O scheduler futuro deverá chamar o mesmo service, funcionando apenas como outro gatilho.

### Justificativa

- A sincronização manual valida o fluxo completo antes da automação.
- `SyncController` fica fino e apenas delega a execução.
- `AgentSyncService` concentra orquestração, upsert, idempotência e registro de auditoria.
- `ExternalAgentClient` fica isolado para evitar acoplamento entre comunicação HTTP externa e domínio persistido.
- `ExternalAgentDto` representa apenas o contrato externo e não é entidade JPA.
- O upsert é feito por `externalId`, preservando `Agent.id` textual vindo da API.
- `SyncExecution` registra auditoria tanto de sucesso quanto de falha.
- As chamadas HTTP externas acontecem fora de transações de banco, evitando transações abertas durante I/O de rede.
- Retries para `429` e `503` possuem limite para evitar loops infinitos.

### Consequências

- O backend já permite testar idempotência de agentes sem scheduler.
- O scheduler futuro deve reutilizar `AgentSyncService` em vez de duplicar regra.
- O retorno do endpoint usa um resumo enriquecido com processados, criados, atualizados, ignorados e horários, melhorando observabilidade durante o desafio.

---

## Decisão 005 — Ajustes técnicos antes de avançar nas próximas sincronizações

**Data:** 2026-05-23

**Status:** Aceita

### Contexto

Após implementar a sincronização manual de agentes, o projeto passou a ter uma fatia vertical real envolvendo controller, service, client externo, banco de dados e auditoria em `SyncExecution`. Antes de avançar para sincronização de localizações, check-ins, geofences e schedulers, foram identificados pequenos débitos técnicos que poderiam dificultar testes e manutenção.

### Decisão

Realizar uma rodada curta de estabilização técnica antes de implementar novas funcionalidades. Os ajustes aplicados foram:

- mover `HealthController` para o pacote `health`, mantendo a organização por feature/domínio;
- remover Lombok das entidades e do build para evitar dependência de annotation processing;
- desligar `spring.jpa.show-sql` por padrão para reduzir ruído em logs;
- extrair `ExternalAgentGateway` para que `AgentSyncService` dependa de uma interface, não diretamente do client HTTP;
- manter `ExternalAgentClient` como implementação baseada em WebClient;
- representar `Retry-After` como dado estruturado em `ExternalApiException`;
- adicionar testes unitários para `AgentSyncService`.

### Justificativa

- A organização por feature torna o projeto mais previsível à medida que novos domínios forem adicionados.
- Remover Lombok das entidades reduz risco de falhas de compilação causadas por configuração de annotation processing.
- Desligar SQL verboso por padrão melhora legibilidade dos logs durante testes manuais e execução local.
- `ExternalAgentGateway` melhora testabilidade e reduz acoplamento entre caso de uso e infraestrutura HTTP.
- O scheduler futuro continuará chamando `AgentSyncService`, sem conhecer detalhes do WebClient.
- `Retry-After` como campo estruturado torna o retry mais claro, menos frágil e mais fácil de testar.
- Os testes unitários protegem as regras centrais da sincronização: criação, atualização por `externalId`, idempotência, auditoria de sucesso, auditoria de falha e conflito de identidade.

### Consequências

- O código ficou um pouco mais explícito, especialmente nas entidades sem Lombok, mas mais previsível para avaliação e manutenção.
- A sincronização de agentes agora possui cobertura automatizada antes de servir como base para schedulers futuros.
- Próximas integrações externas podem seguir o mesmo padrão: `External*Gateway`, client HTTP isolado, service transacional curto e testes unitários do caso de uso.

---

## Decisão 006 — Sincronização manual de localizações com estado atual e histórico

**Data:** 2026-05-23

**Status:** Aceita

### Contexto

O desafio exige rastreamento geográfico atual e histórico de rotas. O endpoint externo confirmado para localizações é `/api/v1/locations` e o retorno real observado possui apenas o formato `{ "data": [...] }`, sem metadados de paginação.

### Decisão

Implementar a sincronização manual de localizações em `POST /api/v1/sync/locations`, usando chamada única ao endpoint externo. O controller apenas delega para `LocationSyncService`, e um scheduler futuro deverá reutilizar esse mesmo service.

### Justificativa

- `ExternalLocationClient` chama `/api/v1/locations` sem barra final e sem paginação artificial, porque o contrato real não trouxe `page`, `totalPages`, `nextPage`, `cursor` ou `hasNext`.
- `ExternalApiRetryPolicy` é reutilizado para manter tratamento consistente de `429` e `503`.
- `Agent` guarda o estado operacional atual do agente, permitindo consultas rápidas para listagem e dashboard.
- `LocationHistory` guarda o histórico de pontos válidos para rotas.
- `lastSeen` da localização externa é usado como `recordedAt`, pois representa o horário do ponto recebido.
- `LocationHistory` continua com `Long` porque a API não fornece ID próprio para localização atual.
- A idempotência do histórico é garantida por `agent_id + recorded_at + source`.
- Leituras com `accuracy > 50` são descartadas completamente: não atualizam `Agent` e não criam `LocationHistory`.
- Leituras com `accuracy = null` são aceitas, porque a regra documentada fala apenas em descartar acurácia superior a 50 metros.
- Localizações de agentes inexistentes são ignoradas (`skipped`) para evitar criação de agentes parciais.
- A sincronização de localização não altera dados cadastrais do agente, como nome, equipe, telefone, e-mail, função ou status ativo.

### Consequências

- Rodar a sincronização de localizações mais de uma vez não deve duplicar pontos no histórico.
- A sincronização de agentes deve ser executada antes da sincronização de localizações, pois localizações de agentes ausentes são ignoradas.
- O histórico fica preparado para rotas futuras sem implementar cálculo Haversine neste momento.

---

## Decisão 007 — Sincronização manual de check-ins com SyncState preparado para incrementalidade

**Data:** 2026-05-23

**Status:** Aceita

### Contexto

O endpoint externo real para buscar check-ins é `GET /api/v1/check-ins`. O endpoint externo `POST /api/v1/sync/check-ins` foi testado e não será usado para buscar eventos, pois não retornou os dados necessários para persistência local.

O retorno real de `GET /api/v1/check-ins` possui o formato `{ "data": [...] }`, sem metadados funcionais de paginação e sem `syncToken` novo.

### Decisão

Implementar a sincronização manual em `POST /api/v1/sync/check-ins`, usando `CheckInSyncService` como caso de uso reutilizável por um scheduler futuro. `SyncState` passa a ser usado para preparar a arquitetura incremental, mas nenhum token local será inventado enquanto a API externa não retornar um token funcional.

### Justificativa

- `ExternalCheckInClient` usa `GET /api/v1/check-ins`, não o `POST /api/v1/sync/check-ins` externo.
- O retorno real não possui paginação funcional, então a chamada é única.
- O retorno real não traz `syncToken` novo; por isso, `SyncState.lastSyncToken` é preservado.
- `SyncExecution.syncTokenBefore` e `SyncExecution.syncTokenAfter` registram tokens quando aplicável.
- `CheckIn.id` é a PK canônica textual, como `seed_ci_010`.
- `externalEventId` continua como identificador único adicional para idempotência.
- Conflitos em que o mesmo `externalEventId` aponta para outro `id` não são sobrescritos silenciosamente.
- Check-ins de agentes inexistentes são ignorados (`skipped`) para evitar criação de agentes parciais.
- Check-ins podem gerar `LocationHistory` quando possuem coordenadas e precisão aceitável.
- `accuracy > 50` não descarta o `CheckIn`; apenas impede a criação de `LocationHistory`.
- O scheduler futuro deverá reutilizar `CheckInSyncService`.

### Consequências

- Rodar o sync de check-ins mais de uma vez não duplica check-ins.
- Eventos operacionais com GPS ruim continuam persistidos em `checkins`.
- O histórico de rotas fica mais completo quando check-ins trazem coordenadas confiáveis.
- A incrementalidade fica preparada sem criar comportamento falso de token.
