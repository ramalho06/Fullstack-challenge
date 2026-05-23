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
