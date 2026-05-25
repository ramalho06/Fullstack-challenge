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

---

## Decisão 008 — Sincronização manual de geofences com GeoJSON bruto

**Data:** 2026-05-23

**Status:** Aceita

### Contexto

O endpoint externo real para geofences é `GET /api/v1/geofences`. Ele funciona sem barra final e retorna apenas `{ "data": [...] }`, sem metadados de paginação.

### Decisão

Implementar a sincronização manual em `POST /api/v1/sync/geofences`, usando chamada única ao endpoint externo. `GeofenceSyncService` concentra o caso de uso e deverá ser reutilizado por um scheduler futuro.

### Justificativa

- `ExternalGeofenceClient` usa `GET /api/v1/geofences`, sem barra final.
- O retorno real não possui paginação, então não será criada paginação artificial.
- `ExternalApiRetryPolicy` é reutilizado para manter tratamento consistente de `429` e `503`.
- `Geofence.id` é `String` porque a API retorna IDs textuais como `seed_geo_003`.
- O upsert é feito por `externalId`, conforme o contrato da API.
- Conflitos em que `externalId` existente aponta para `id` diferente não são sobrescritos silenciosamente.
- `coordinatesJson` é salvo como `TEXT`/`String`, sem normalização geométrica neste passo.
- `assignedTeams` é salvo como `String`, sem criação de tabela de equipes.

### Consequências

- Rodar o sync de geofences mais de uma vez não duplica registros.
- Geofencing visual, cálculo espacial e normalização de geometrias ficam para etapas futuras.
- O modelo preserva o contrato externo e mantém baixo risco técnico para esta fase.

---

## Decisão 009 — Schedulers automáticos reutilizando os services de sincronização manual

**Data:** 2026-05-24

**Status:** Aceita

### Contexto

O desafio exige quatro schedulers automáticos: agentes, localizações, check-ins e geofences. Antes desta etapa, cada sincronização já existia como endpoint manual e possuía um service próprio com regra de negócio, persistência, retries e auditoria em `SyncExecution`.

### Decisão

Implementar os quatro schedulers como gatilhos automáticos que reutilizam os mesmos services dos endpoints manuais:

- `AgentSyncService`;
- `LocationSyncService`;
- `CheckInSyncService`;
- `GeofenceSyncService`.

Os intervalos e delays iniciais são configuráveis por `application.yml`, sob o prefixo `app.schedulers`.

### Justificativa

- Nenhuma regra de negócio é duplicada no scheduler.
- `SyncExecution` continua sendo responsabilidade dos services de sincronização.
- `initialDelay` evita disparar todos os syncs ao mesmo tempo na subida da aplicação.
- `AtomicBoolean` impede sobreposição da mesma rotina dentro da instância atual.
- Falhas são logadas pelo scheduler e não derrubam a aplicação.
- `app.schedulers.enabled=false` permite desabilitar os gatilhos automáticos sem remover os endpoints manuais.
- ShedLock não foi usado porque o projeto roda em instância única neste momento.

### Consequências

- A aplicação passa a sincronizar automaticamente dados externos.
- Endpoints manuais continuam disponíveis para testes e reprocessamento controlado.
- Em ambiente com múltiplas instâncias, ShedLock ou outro lock distribuído seria a evolução natural para evitar execução duplicada entre nós diferentes.

---

## Decisão 010 — Endpoints de consulta e CRUD expõem o domínio via DTOs

**Data:** 2026-05-24

**Status:** Aceita

### Contexto

Depois das sincronizações manuais e automáticas, o frontend precisa consumir dados estáveis do backend. Expor entidades JPA diretamente nos controllers criaria acoplamento com o modelo de persistência, risco de vazamento de campos internos e maior chance de problemas com relacionamentos lazy.

### Decisão

Implementar endpoints de consulta e gestão usando DTOs de request e response. O CRUD completo será limitado a `Agent`, enquanto localizações, check-ins e geofences terão endpoints de consulta. O registro manual de check-in será permitido por `POST /api/v1/check-ins`.

### Justificativa

- `Agent` é o único recurso com CRUD completo neste passo, porque o desafio pede gestão completa de agentes.
- Localizações e geofences são inicialmente alimentadas pela sincronização externa e expostas como consulta para o frontend.
- Check-ins possuem consulta e criação manual, porque registro manual de check-in é requisito funcional do desafio.
- Agentes criados localmente recebem `id` no formato `local_agent_<uuid>` e `externalId` no formato `local-ext-agent_<uuid>`.
- O `externalId` local é gerado pelo backend para manter a constraint `NOT NULL UNIQUE` sem obrigar o frontend a conhecer detalhes técnicos de sincronização.
- `DELETE /api/v1/agents/{id}` faz soft delete, definindo `active=false` e `status=OFFLINE`, para preservar histórico e relacionamentos.
- `PUT /api/v1/agents/{id}` altera apenas campos cadastrais e operacionais editáveis manualmente: nome, função, equipe, telefone, e-mail, ativo e status.
- Campos controlados por sincronização, como localização atual, bateria, `lastSeen`, `externalId` e timestamps externos, não são editáveis via CRUD.
- Controllers permanecem finos: validam entrada, delegam para services e retornam DTOs.
- Endpoints paginados usam um DTO próprio (`PageResponse`) para evitar expor a serialização interna de `PageImpl` do Spring e entregar um contrato JSON mais estável ao frontend.
- O tratamento global de erros padroniza respostas de 404, validação, argumentos inválidos e falhas inesperadas.
- Rota do dia e cálculo Haversine ficam para o próximo passo, porque exigem regras próprias sobre histórico, ordenação e distância entre pontos.

### Consequências

- O frontend pode começar a consumir endpoints reais sem depender de entidades JPA.
- A API fica mais estável para evolução futura, mesmo que o modelo interno mude.
- Agentes locais podem ser criados sem conflitar com IDs canônicos da API externa.
- O histórico de check-ins e localizações permanece preservado mesmo após soft delete de agente.
- Ainda será necessário implementar a rota do dia e o cálculo de distância em uma etapa específica.

---

## Decisão 011 — Padronização de erros da API pública

**Data:** 2026-05-24

**Status:** Aceita

### Contexto

Com os endpoints públicos de consulta, CRUD de agentes e check-in manual expostos para o frontend, o contrato de erro precisa ser previsível. Respostas montadas de forma ad hoc dificultam tratamento no frontend e deixam a API menos clara durante avaliação técnica.

### Decisão

Padronizar respostas de erro com `ApiErrorResponse`, sempre no formato:

```json
{
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Agent not found",
    "details": "Agent id=seed_agent_999"
  }
}
```

### Justificativa

- Recursos inexistentes retornam `404` com `RESOURCE_NOT_FOUND`.
- Erros de validação retornam `422` com `VALIDATION_ERROR`.
- Requisições inválidas retornam `400` com `BAD_REQUEST`.
- Conflitos de domínio passam a ter suporte explícito com `409` e `CONFLICT`.
- Erros inesperados retornam `500` com `INTERNAL_ERROR`, sem expor stacktrace.
- `ExternalApiException` mantém tratamento específico para falhas externas, incluindo `429` quando a API externa aplica rate limiting.
- Um DTO explícito de erro facilita testes e consumo pelo frontend.

### Consequências

- O frontend pode tratar erros por `error.code`, sem depender de texto livre.
- Novos conflitos de domínio podem usar `ConflictException` sem alterar o contrato HTTP.
- A API pública fica mais estável antes da implementação da rota do dia e do cálculo Haversine.

---

## Decisão 012 — Histórico de rota do dia com Haversine

**Data:** 2026-05-24

**Status:** Aceita

### Contexto

O desafio exige histórico completo de rota do dia e cálculo de distância. O sistema já consolida pontos geográficos em `LocationHistory`, alimentado por sincronização de localizações e por check-ins com coordenadas válidas.

### Decisão

Implementar `GET /api/v1/agents/{id}/route?date=YYYY-MM-DD` construindo a rota exclusivamente a partir de `LocationHistory`. A distância entre pontos consecutivos válidos será calculada com a fórmula de Haversine.

### Justificativa

- `LocationHistory` é a fonte consolidada de pontos geográficos do sistema.
- `CheckIn` não será consultado diretamente para evitar duplicidade, pois check-ins com coordenadas válidas já podem gerar `LocationHistory`.
- `date` será interpretado em `America/Sao_Paulo`, porque representa uma data operacional local.
- Timestamps continuam persistidos como `Instant`, e o intervalo local é convertido para `Instant` antes da consulta.
- A consulta usa início inclusivo e fim exclusivo: `recordedAt >= start` e `recordedAt < end`.
- Pontos com `accuracy > 50` são filtrados defensivamente na resposta, sem alterar o banco.
- Pontos com `accuracy = null` são aceitos, seguindo a regra já adotada nas sincronizações.
- Todos os `LocationSource` entram na rota: `GPS_SYNC`, `MANUAL_CHECKIN` e `EVENT_SYNC`.
- A distância total é a soma das distâncias entre pontos consecutivos válidos.
- Se o agente existir sem pontos no dia, a resposta é `200 OK` com lista vazia e distância total `0.00`.
- `GeoDistanceCalculator` fica em `common/geo` para poder ser reutilizado por check-ins, geofencing ou relatórios futuros.

### Consequências

- O frontend passa a ter um endpoint direto para exibir o percurso diário.
- A regra de distância fica testável e isolada do controller.
- Não há dependência de PostGIS, biblioteca geoespacial ou nova tabela neste passo.
- Um filtro por `source` pode ser adicionado futuramente sem mudar a fonte principal da rota.

---

## Decisão 013 — Monitoramento operacional das sincronizações

**Data:** 2026-05-24

**Status:** Aceita

### Contexto

O sistema já persistia `SyncExecution` para auditar cada sincronização manual ou automática. Para o frontend e para a avaliação técnica, porém, não basta gravar esse histórico: é necessário expor uma leitura operacional clara sobre execuções, falhas, últimas rodadas e configuração dos schedulers.

### Decisão

Implementar endpoints de monitoramento em `GET /api/v1/sync/executions`, `GET /api/v1/sync/executions/latest` e `GET /api/v1/sync/status`, usando DTOs e um `SyncMonitoringService` dedicado.

### Justificativa

- `GET /api/v1/sync/executions` lista o histórico de `SyncExecution` com filtros por `syncType`, `status` e paginação.
- `GET /api/v1/sync/executions/latest` retorna no máximo uma última execução por tipo, em ordem lógica: agentes, localizações, check-ins, geofences e full sync.
- `GET /api/v1/sync/status` consolida o estado operacional das sincronizações.
- `overallStatus` usa `HEALTHY`, `WARNING` e `DEGRADED`.
- Uma última execução `FAILED` torna o status `DEGRADED`.
- Uma última execução `PARTIAL_SUCCESS`, sem falhas, torna o status `WARNING`.
- Na ausência de falhas conhecidas, o status permanece `HEALTHY`.
- Dados dos schedulers são expostos a partir de `SchedulerProperties`, sem tentar detectar execução em andamento neste passo.
- `errorMessage` é resumido para evitar exposição de stacktrace.
- Entidades JPA não são retornadas diretamente nos endpoints de monitoramento.

### Consequências

- O frontend ganha base para construir um painel de monitoramento.
- O avaliador consegue verificar rapidamente quando as sincronizações rodaram e se falharam.
- A regra de cálculo de status fica isolada e testável.
- Nenhuma regra de sincronização ou scheduler precisou ser alterada.

---

## Decisão 014 — Documentação da API com Swagger/OpenAPI

**Data:** 2026-05-24

**Status:** Aceita

### Contexto

O backend já possui endpoints públicos para CRUD de agentes, consultas operacionais, sincronizações manuais, rota diária e monitoramento de sync. Para facilitar avaliação técnica e preparar o consumo pelo frontend, o contrato HTTP precisa estar navegável e documentado.

### Decisão

Adicionar documentação OpenAPI com `springdoc-openapi-starter-webmvc-ui`, mantendo Swagger UI em `/swagger-ui/index.html` e o JSON OpenAPI em `/v3/api-docs`.

### Justificativa

- A aplicação expõe controllers Spring MVC, portanto foi usado o starter WebMVC do springdoc.
- A versão `2.7.0` foi fixada após validação local com Spring Boot `3.4.1`; versões mais novas do springdoc apresentaram conflito de resource handler com o parser de paths do Spring MVC neste projeto.
- Endpoints foram agrupados por domínio: Health, Agents, Locations, Check-ins, Geofences, Routes, Sync commands e Sync monitoring.
- Endpoints de sincronização manual também foram documentados porque são parte importante do fluxo de integração.
- Endpoints de monitoramento foram documentados para evidenciar maturidade operacional.
- `ApiErrorResponse` foi documentado como contrato padrão de erro.
- Exemplos foram adicionados apenas nos endpoints principais para manter a documentação útil sem excesso de anotações.
- Não foi configurado `securityScheme`, porque o backend ainda não possui autenticação.
- Segredos, API Key externa e variáveis de ambiente não são expostos no Swagger.

### Consequências

- O avaliador pode navegar e testar a API pelo Swagger UI.
- O frontend passa a ter uma referência clara de requests, responses e erros.
- A documentação segue o contrato público de DTOs, sem expor entidades JPA como modelo da API.
- Uma futura autenticação poderá adicionar `securityScheme` de forma explícita quando existir implementação real.

---

## Decisão 015 — Dockerização do backend com MySQL

**Data:** 2026-05-24

**Status:** Aceita

### Contexto

O avaliador precisa conseguir subir o backend completo com baixo atrito. Até aqui, o `docker-compose.yml` subia apenas o MySQL, exigindo que o backend fosse executado localmente com Java e variáveis de ambiente configuradas no terminal ou na IDE.

### Decisão

Adicionar um `Dockerfile` multi-stage para o backend e ajustar o `docker-compose.yml` para subir `mysql` e `backend` com `docker compose up --build`.

### Justificativa

- O backend usa build multi-stage: uma etapa com JDK 17 e Maven Wrapper para gerar o JAR, e uma etapa runtime com Java 17 para executar a aplicação.
- Naquele momento, o frontend ainda não foi incluído no Docker porque estava no setup inicial local; a dockerização completa foi adicionada depois, quando as telas reais ficaram prontas.
- O backend continua configurado por variáveis de ambiente, sem criar profile Spring específico para Docker.
- O MySQL possui `healthcheck` para indicar quando está realmente pronto para conexões.
- O backend usa `depends_on` com `condition: service_healthy`, evitando que ele tente conectar antes do MySQL estar saudável.
- Flyway roda automaticamente ao subir o backend, aplicando migrations antes da aplicação ficar pronta.
- Segredos ficam fora do repositório e devem ser definidos no `.env`.
- Dentro do Docker, o backend acessa o banco pelo hostname `mysql`, não por `localhost`, porque cada serviço roda em seu próprio container.

### Consequências

- O avaliador conseguia subir banco e backend com um único comando nessa etapa.
- O ambiente Docker fica mais próximo de uma implantação real simples.
- A execução local fora do Docker continua possível usando as mesmas variáveis de ambiente.
- Um serviço de frontend foi adicionado posteriormente ao compose após a implementação das telas reais.

---

## Decisão 016 — Congelamento do backend antes do frontend

**Data:** 2026-05-24

**Status:** Aceita

### Contexto

O backend já cobre as funcionalidades obrigatórias principais do desafio: sincronizações manuais e automáticas, CRUD de agentes, consultas públicas, check-ins manuais, rota do dia com Haversine, monitoramento operacional, documentação OpenAPI, tratamento padronizado de erros e execução containerizada com MySQL.

### Decisão

Considerar o backend congelado para novas features antes do início do frontend. A partir deste ponto, alterações no backend devem se limitar a correções de bug crítico, documentação ou pequenos ajustes necessários para consumo pelo frontend.

### Justificativa

- O backend foi revisado antes do início do frontend para reduzir risco de integração.
- A validação final inclui testes automatizados, Docker, Swagger, OpenAPI JSON, Flyway, endpoints principais e documentação.
- `docs/api-examples.md` centraliza comandos de uso da API sem deixar o README excessivamente longo.
- Limitações conhecidas foram documentadas de forma explícita para transparência técnica.
- Congelar contratos evita que o frontend seja construído sobre uma API instável.

### Consequências

- O próximo foco do projeto passa a ser o frontend.
- Novas features backend ficam fora do escopo imediato.
- Bugs críticos e ajustes pequenos continuam permitidos.
- Mudanças de contrato HTTP devem ser evitadas, salvo necessidade clara.

---

## Decisão 017 — Setup inicial do frontend com Next.js 16

**Data:** 2026-05-24

**Status:** Aceita

### Contexto

Com o backend validado e congelado para novas features, o projeto passa a precisar de uma base frontend estável para consumir os endpoints públicos já implementados. O objetivo deste passo é preparar a fundação técnica sem iniciar dashboard real, mapa, CRUD visual ou formulários completos.

### Decisão

Criar o frontend do zero em `frontend/` usando Next.js 16, TypeScript, App Router, Tailwind CSS, shadcn/ui, TanStack Query, React Hook Form e Zod. O package manager escolhido foi `npm` para reduzir atrito de execução para o avaliador.

### Justificativa

- Next.js 16 com App Router atende ao requisito do desafio e favorece rotas organizadas por arquivo.
- Server Components serão o padrão; Client Components ficam restritos a providers, interações, formulários, TanStack Query e componentes que dependam do browser.
- TanStack Query será usado para estado servidor, cache e invalidação de dados vindos do backend.
- shadcn/ui fornece uma base visual consistente sem criar um design system próprio neste momento.
- React Hook Form e Zod foram instalados desde o setup para preparar os formulários dos próximos passos.
- `NEXT_PUBLIC_API_BASE_URL` centraliza a URL do backend para o frontend.
- O backend recebeu um ajuste mínimo de CORS para permitir `http://localhost:3000`, necessário para integração via navegador.

### Consequências

- O frontend já possui layout base, navegação e páginas placeholder.
- As próximas etapas podem focar telas reais em vez de infraestrutura.
- O backend continua congelado para novas features; o CORS é tratado como ajuste necessário de integração.
- A dockerização do frontend foi adiada neste momento para evitar complexidade antes das telas reais; ela foi adicionada depois como fechamento da entrega fullstack.

---

## Decisão 018 — Dashboard frontend consumindo APIs reais

**Data:** 2026-05-25

**Status:** Aceita

### Contexto

Depois do setup inicial do frontend, era necessário provar a integração real com o backend antes de avançar para telas mais complexas como CRUD visual, mapa e formulários. O dashboard é a primeira tela útil para validar consumo de APIs, estados de carregamento, estados de erro e atualização periódica.

### Decisão

Implementar o dashboard operacional mínimo na rota `/`, consumindo endpoints reais do backend para agentes, localizações atuais, status de sincronização e últimas execuções.

### Justificativa

- A rota `/` passa a ser a visão operacional principal do sistema.
- TanStack Query foi usado para estado servidor, cache e refetch periódico.
- `refetchInterval` substitui SSE/WebSocket neste momento, mantendo o escopo simples e suficiente para uma visão operacional inicial.
- Cards e tabelas foram priorizados antes de gráficos e mapa, porque validam a integração com menor complexidade.
- O dashboard é somente leitura e observabilidade; ações manuais de sync ficam fora deste passo.
- Leaflet e mapa interativo permanecem para uma etapa posterior.
- shadcn/ui foi usado para cards, tabelas, badges e skeletons, mantendo consistência visual com a base do frontend.

### Consequências

- O frontend passa a demonstrar comunicação real com o backend.
- O avaliador consegue ver métricas e dados operacionais sem depender do Swagger ou de `curl`.
- A arquitetura de services e hooks fica preparada para as próximas telas.
- O dashboard ainda não executa ações, não exibe mapa e não implementa formulários.

---

## Decisão 019 — Telas essenciais com formulários validados

**Data:** 2026-05-25

**Status:** Aceita

### Contexto

Depois do dashboard, o frontend precisava deixar de ser apenas observabilidade inicial e passar a cobrir os fluxos principais do usuário: gestão de agentes, registro manual de check-ins, consulta de geofences e monitoramento operacional de sincronização.

### Decisão

Implementar as telas `/agents`, `/check-ins`, `/geofences` e `/sync` consumindo APIs reais do backend, usando TanStack Query para queries/mutations e React Hook Form + Zod nos formulários.

### Justificativa

- `/agents` concentra a gestão visual de agentes, incluindo criação, edição e desativação.
- Criação e edição de agentes usam Dialog para reduzir complexidade de rotas e manter o fluxo rápido.
- Formulários usam React Hook Form + Zod para validação local previsível.
- Mutations usam TanStack Query e invalidam caches relacionados após sucesso.
- Sonner é usado para feedback de sucesso e erro nas ações do usuário.
- DELETE de agente é apresentado como “Desativar”, porque o backend usa soft delete.
- `/check-ins` permite consulta e registro manual de check-ins com seleção de agente via Select.
- `/geofences` é apenas consulta, preservando geometrias como JSON bruto.
- `/sync` é leitura/monitoramento, sem comandos manuais neste passo.
- Paginação simples com “Anterior” e “Próxima” foi escolhida por prazo e clareza.
- Rotas de detalhe foram adiadas para evitar expansão de escopo.
- Leaflet, mapa e geofencing visual ficam para etapa posterior.

### Consequências

- O frontend cobre os principais fluxos funcionais do desafio sem depender do Swagger.
- Os contratos públicos do backend foram reaproveitados sem alteração.
- A experiência já possui estados de loading, erro e vazio nas telas principais.
- A navegação continua simples, sem rotas de detalhe ou ações operacionais de sync manual.

---

## Decisão 020 — Mapa com Leaflet para rastreamento visual

**Data:** 2026-05-25

**Status:** Aceita

### Contexto

Após dashboard e telas essenciais, o projeto precisava de um diferencial visual para demonstrar rastreamento geográfico. O backend já expunha localizações atuais e rota do dia, então o frontend poderia consumir esses dados sem alterar contratos ou criar lógica GIS avançada.

### Decisão

Implementar a rota `/map` usando Leaflet e react-leaflet para exibir agentes no mapa e desenhar a rota do dia com Polyline.

### Justificativa

- Leaflet foi escolhido por ser uma biblioteca madura e simples para visualização geográfica no frontend.
- `react-leaflet` foi usado para integração idiomática com React.
- O mapa fica em `/map`, mantendo o dashboard limpo.
- `CircleMarker` foi escolhido em vez de `Marker` para evitar problemas de ícones padrão do Leaflet no Next.js e facilitar cor por status.
- Localizações atuais vêm de `GET /api/v1/locations`.
- A atualização periódica usa TanStack Query com `refetchInterval` de 30 segundos, sem SSE/WebSocket.
- A rota do dia usa `GET /api/v1/agents/{id}/route?date=YYYY-MM-DD`.
- `Polyline` desenha o trajeto retornado pelo backend.
- Geofences visuais foram deixadas para passo posterior para evitar parse de `coordinatesJson` e lógica espacial adicional.

### Consequências

- O frontend passa a ter uma visualização geográfica real dos agentes.
- A rota do dia reaproveita o cálculo Haversine já implementado no backend.
- O mapa continua simples e focado em leitura; clusterização, desenho manual e geofencing visual ficaram fora desta decisão inicial.
- O uso de Leaflet exige Client Components e carregamento sem SSR para evitar dependência de APIs do browser no servidor.

## Decisão 021 — Geofencing visual no mapa

**Data:** 2026-05-25

**Status:** Aceita

### Contexto

Depois que o mapa passou a exibir agentes e rota do dia, faltava visualizar as áreas operacionais sincronizadas pelo backend. As geofences já estavam persistidas com `coordinatesJson`, mas esse campo é propositalmente bruto para evitar acoplar o backend a uma biblioteca GIS neste desafio.

### Decisão

Renderizar geofences diretamente na rota `/map`, consumindo `GET /api/v1/geofences?page=0&size=100` e interpretando `coordinatesJson` no frontend.

### Justificativa

- A rota `/map` é o lugar natural para visualizar agentes, rotas e áreas operacionais sem criar uma nova página.
- A API envia coordenadas como `[longitude, latitude]`, enquanto o Leaflet exige `[latitude, longitude]`; por isso a conversão é explícita no frontend.
- Geofences do tipo `CIRCLE` são renderizadas com `Circle`.
- Geofences do tipo `POLYGON` são renderizadas com `Polygon`.
- O parser é defensivo: JSON inválido ou geometria incompleta é ignorado para não quebrar o mapa.
- Um toggle permite ocultar geofences quando o usuário quiser focar apenas em agentes ou rota do dia.

### Consequências

- O mapa passa a entregar geofencing visual sem alterar backend ou contrato de API.
- A solução continua focada em visualização, sem alertas reais de entrada/saída.
- Não foi implementada lógica GIS avançada, edição manual de áreas, clusterização ou cálculo espacial no frontend.

## Decisão 022 — Polimento final e fechamento da entrega

**Data:** 2026-05-25

**Status:** Aceita

### Contexto

Após a implementação do backend, frontend, mapa e geofencing visual, o projeto entrou em fase de fechamento. Nesta etapa, o objetivo deixa de ser criar novas funcionalidades e passa a ser garantir que a entrega esteja clara, executável, documentada e honesta sobre seus limites.

### Decisão

Congelar novas features e focar o polimento final em validação, documentação e pequenos ajustes de consistência.

### Justificativa

- O backend já cobre os requisitos principais: sincronizações manuais, schedulers, CRUD de agentes, check-ins manuais, rota do dia, Haversine, monitoramento, Swagger, Docker e tratamento padronizado de erros.
- O frontend consome APIs reais para dashboard, agentes, check-ins, geofences, sincronização e mapa.
- Os diferenciais priorizados foram Docker, testes automatizados, Swagger/OpenAPI, Leaflet e geofencing visual.
- SSE/WebSocket, Circuit Breaker com Resilience4j, autenticação e alertas reais de geofence ficam como evolução futura.
- A validação final deve incluir testes do backend, build do frontend, Docker, Swagger, endpoints principais e fluxo visual ponta a ponta.

### Consequências

- O projeto fica estável para avaliação técnica.
- Mudanças futuras devem ser correções críticas, ajustes de documentação ou melhorias pequenas necessárias para a entrega.
- Grandes refatorações e novas features ficam fora do escopo final para reduzir risco antes da apresentação.

---

## Decisão 023 — Dockerização completa do frontend

**Data:** 2026-05-25

**Status:** Aceita

### Contexto

O diferencial do desafio cita dockerização completa. O projeto já subia MySQL e backend via `docker compose up --build`, mas o frontend ainda precisava ser executado localmente com `npm run dev`. Com as telas reais já implementadas, fazia sentido fechar a entrega com o stack fullstack containerizado.

### Decisão

Adicionar `frontend/Dockerfile`, incluir o serviço `frontend` no `docker-compose.yml` e permitir que `docker compose up --build` suba MySQL, backend e frontend.

### Justificativa

- O frontend usa Dockerfile multi-stage com Node 20: uma etapa instala dependências, outra executa `npm run build` e a etapa final roda `npm run start`.
- `NEXT_PUBLIC_API_BASE_URL` é definido como build arg e variável de ambiente, com padrão `http://localhost:8080`.
- A URL pública do backend continua apontando para `localhost:8080`, porque as chamadas do frontend acontecem no navegador do avaliador.
- O backend já libera CORS para `http://localhost:3000`.
- O frontend não recebe segredos; apenas variáveis públicas com prefixo `NEXT_PUBLIC_`.
- A execução local com `npm run dev` continua disponível para desenvolvimento.

### Consequências

- O avaliador consegue subir a aplicação fullstack com um único comando.
- O README passa a documentar Docker como fluxo principal para MySQL, backend e frontend.
- A imagem do frontend é simples e prioriza previsibilidade de entrega; otimizações como `output: "standalone"` podem ser avaliadas futuramente, mas não são necessárias para o desafio.
