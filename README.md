# FlowGuard Service

Microserviço de **rate limiting** centralizado, construído com Spring Boot 4 + Redis.
Outros serviços consultam o FlowGuard antes de processar uma requisição para saber se o
cliente ainda está dentro do limite permitido.

## Como funciona

- O algoritmo atual é **janela fixa (fixed window)**: um contador por
  `clientId + endpoint + httpMethod` no Redis, com TTL igual à janela da regra.
- O incremento do contador e a definição do TTL acontecem em um **script Lua atômico**,
  evitando condição de corrida e chaves órfãs sem expiração.
- As regras podem ser **customizadas por cliente/endpoint/método** (armazenadas no Redis)
  ou cair na **regra default** configurada em `application.yml`
  (`rate-limit.default-rule`).

## Como rodar

```bash
# sobe Redis + serviço
docker compose up --build

# ou, com Redis já rodando localmente:
./gradlew bootRun
```

Swagger UI: http://localhost:8080/swagger-ui.html

## API

### Verificação de limite

`POST /api/v1/rate-limit/check`

```json
{ "clientId": "cliente-a", "endpoint": "/api/pedidos", "httpMethod": "GET" }
```

Resposta:

```json
{ "allowed": true, "decision": "ALLOWED", "remainingRequests": 99, "resetInSeconds": 60 }
```

### Administração de regras

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/rules` | Cria/atualiza uma regra (`limit` e `windowSeconds` devem ser ≥ 1) |
| `GET` | `/rules?clientId=&endpoint=&method=` | Busca uma regra específica |
| `GET` | `/rules/all` | Lista todas as regras customizadas |
| `DELETE` | `/rules?clientId=&endpoint=&method=` | Remove uma regra |

## Roadmap (próximos passos)

Ideias para continuar a evolução do projeto, em ordem sugerida:

1. **Proteger os endpoints administrativos** — hoje `/rules` é aberto. Adicionar
   `spring-boot-starter-security` com API key ou basic auth para o admin.
2. **Tratamento de erros padronizado** — um `@RestControllerAdvice` retornando
   Problem Details (RFC 9457), sem stack trace no corpo da resposta.
3. **Algoritmos alternativos** — o fixed window permite rajadas na virada da janela.
   Implementar **sliding window** (ZSET) ou **token bucket** como estratégias
   selecionáveis por regra (`algorithm` na regra).
4. **Métricas e observabilidade** — contadores Micrometer (permitidos x bloqueados por
   cliente/endpoint) expostos no Actuator/Prometheus.
5. **Testes de integração com Testcontainers** — subir Redis real nos testes e cobrir o
   script Lua e o repositório de regras.
6. **Resiliência** — decidir o comportamento quando o Redis está fora
   (fail-open x fail-closed) e cobrir com circuit breaker (Resilience4j).
7. **Modo gateway/filtro** — além da API de consulta, oferecer uma lib cliente ou um
   filtro para Spring Cloud Gateway que consome o FlowGuard automaticamente.
8. **TTL/auditoria de regras** — regras com expiração opcional e histórico de alterações.
