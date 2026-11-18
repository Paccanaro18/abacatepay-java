# 🥑 abacatepay-java — NÃO OFICIAL

SDK em Java e starter para Spring Boot da [API v2 do AbacatePay](https://docs.abacatepay.com).

> ⚠️ **Projeto não oficial e independente.** Não é criado, mantido nem endossado pelo AbacatePay.
> O nome "AbacatePay" pertence aos respectivos donos. Em desenvolvimento: a API deste SDK pode mudar antes da 1.0.

## O que é

O AbacatePay é um gateway de pagamentos brasileiro focado em Pix. Ele expõe uma API HTTP, e a documentação lista SDKs oficiais
em várias linguagens. Para JVM existem os oficiais em Java e Kotlin, mas eles foram feitos para a **API v1 (legado)**.
Este projeto cobre a **API v2** na JVM, com integração nativa ao Spring Boot.

Ele resolve o que todo mundo acaba reescrevendo à mão:

- montar a requisição e ler o envelope `{success, data, error}`;
- traduzir erros HTTP em exceções tipadas;
- verificar a assinatura dos webhooks (secret + HMAC-SHA256) e expor um endpoint pronto.

## Módulos

| Módulo | Para que serve |
|---|---|
| `abacatepay-core` | Cliente HTTP, modelos, erros e verificação de webhook. Java puro, sem Spring. |
| `abacatepay-spring-boot-autoconfigure` | Configura tudo a partir do `application.properties` e registra o endpoint de webhook. |
| `abacatepay-spring-boot-starter` | A dependência única que você adiciona no projeto (agrega os dois acima). |

## Funcionalidades

| Disponível | Planejado |
|---|---|
| Criar, consultar e listar checkouts (com filtros e paginação por cursor) | Clientes, produtos e assinaturas |
| Erros tipados (401, 400, 404, 422, 429, 5xx, conexão) | |
| Verificação de webhook (secret + HMAC-SHA256) | Publicação no Maven Central |
| Endpoint de webhook que publica `ApplicationEvent` | |
| Enums tolerantes a valores novos da API | |
| Deduplicação opcional de eventos por `id` | |

## Requisitos

- Java 17+
- Spring Boot 3.x (só para o starter; o `core` não depende de Spring)
- Uma conta no AbacatePay e uma chave de API

## Instalação

Ainda não está no Maven Central. Por enquanto, instale localmente:

```bash
git clone https://github.com/paccanaro18/abacatepay-java.git
cd abacatepay-java
mvn install
```

Depois, no seu projeto:

```xml
<dependency>
  <groupId>io.github.paccanaro18</groupId>
  <artifactId>abacatepay-spring-boot-starter</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Configuração

```properties
abacatepay.api-key=${ABACATEPAY_API_KEY}
abacatepay.webhook.secret=${ABACATEPAY_WEBHOOK_SECRET}

# opcionais
abacatepay.base-url=https://api.abacatepay.com/v2
abacatepay.webhook.path=/abacatepay/webhook
abacatepay.webhook.enabled=true
```

- O ambiente é definido pela chave: `abc_dev_...` usa o **modo de teste**; `abc_prod_...`, a **produção**.
- Use variável de ambiente. **Nunca** coloque a chave no código nem no Git.
- Sem `abacatepay.api-key`, o bean `AbacatePay` não é criado. Sem `abacatepay.webhook.secret`, o endpoint de webhook não é ativado.
- Se você declarar o seu próprio bean `AbacatePay` ou `WebhookVerifier`, o seu tem prioridade.

## Criar um checkout

Valores em **centavos**. O `id` do item é o id de um produto já cadastrado no AbacatePay.

```java
@Service
class CobrancaService {

    private final AbacatePay abacatePay;

    CobrancaService(AbacatePay abacatePay) {
        this.abacatePay = abacatePay;
    }

    String criarLinkDePagamento(String pedidoId) {
        Checkout checkout = abacatePay.checkouts().create(
                CreateCheckoutRequest.builder()
                        .item("prod_abc123", 1)
                        .methods(PaymentMethod.PIX)
                        .externalId(pedidoId)
                        .returnUrl("https://meusite.com/pedidos")
                        .completionUrl("https://meusite.com/obrigado")
                        .build());

        return checkout.url(); // redirecione o cliente para este link
    }
}
```

Campos opcionais não informados não são enviados à API. A requisição é validada antes do envio
(item obrigatório, quantidade mínima 1, ao menos um método se `methods` for informado).

### Consultar e listar

```java
Checkout checkout = abacatePay.checkouts().get("bill_abc123xyz");
Checkout porPedido = abacatePay.checkouts().getByExternalId("pedido-123");

Page<Checkout> pagina = abacatePay.checkouts().list(
        ListCheckoutsRequest.builder()
                .status(CheckoutStatus.PAID)
                .startDate(LocalDate.of(2026, 1, 1))
                .limit(50)
                .build());

while (true) {
    pagina.items().forEach(c -> System.out.println(c.id()));
    if (!pagina.hasMore()) {
        break;
    }
    pagina = abacatePay.checkouts().list(
            ListCheckoutsRequest.builder().limit(50).after(pagina.next()).build());
}
```

A listagem devolve só checkouts de pagamento único, do mais novo para o mais antigo. `limit` vai de 1 a 100.
Ao paginar, repita os mesmos filtros e acrescente `after(pagina.next())`. Um checkout inexistente lança `AbacatePayNotFoundException`.

### Tratando erros

Toda falha vira uma subclasse de `AbacatePayException`, que traz o `statusCode()`:

| Exceção | Quando |
|---|---|
| `AbacatePayAuthenticationException` | 401: chave inválida |
| `AbacatePayBusinessException` | 400: regra de negócio |
| `AbacatePayNotFoundException` | 404: recurso não encontrado |
| `AbacatePayValidationException` | 422: requisição fora do schema |
| `AbacatePayRateLimitException` | 429: limite de requisições |
| `AbacatePayServerException` | 5xx: falha no AbacatePay |
| `AbacatePayConnectionException` | rede ou timeout (status 0) |

```java
try {
    abacatePay.checkouts().create(request);
} catch (AbacatePayValidationException e) {
    // corrigir os dados enviados
} catch (AbacatePayException e) {
    log.warn("AbacatePay respondeu {}: {}", e.statusCode(), e.getMessage());
}
```

## Receber webhooks

Com `abacatepay.webhook.secret` definido e Spring MVC no projeto, o endpoint `POST /abacatepay/webhook` já fica ativo.
Cadastre essa URL e o **mesmo secret** no painel do AbacatePay.

```java
@Component
class PagamentoListener {

    @EventListener
    void aoReceberWebhook(WebhookEvent event) {
        if (event.type() != WebhookEventType.CHECKOUT_COMPLETED) {
            return;
        }
        CheckoutEventData dados = event.checkoutData();
        String pedido = dados.checkout().externalId();
        // marcar o pedido como pago
    }
}
```

### Como o endpoint se comporta

1. Lê o corpo como **bytes**, sem converter antes.
2. Confere o **secret** (header `X-Webhook-Secret` ou query `?webhookSecret=`) e a **assinatura** HMAC-SHA256
   (`X-Webhook-Signature`), com comparação em tempo constante.
3. Só depois interpreta o evento e o publica como evento do Spring.

| Situação | Resposta |
|---|---|
| Secret ou assinatura inválidos ou ausentes | `401`, sem corpo (não revela qual verificação falhou) |
| Assinatura válida, mas corpo inválido | `400` |
| Seu listener lança exceção | `500`: o AbacatePay **tenta de novo** |
| Tudo certo | `200` |

> **Seu listener precisa ser idempotente.** Como falhas provocam nova tentativa, o mesmo `event.id()` pode chegar mais de uma vez.

### Deduplicação opcional

Declare um bean `ProcessedEventStore` e o endpoint passa a ignorar eventos cujo `id` já foi processado:
responde `200` sem publicar de novo. O evento só é marcado **depois** que o seu listener termina sem erro,
então uma falha continua provocando nova tentativa.

```java
@Bean
ProcessedEventStore processedEventStore() {
    return new InMemoryProcessedEventStore();
}
```

O `InMemoryProcessedEventStore` guarda os últimos 10.000 ids na memória: some ao reiniciar e não é
compartilhado entre instâncias. Com mais de uma instância, implemente a interface com banco ou Redis.
Duas entregas simultâneas do mesmo evento ainda podem passar; o listener idempotente continua sendo a garantia final.

Em desenvolvimento, o AbacatePay não consegue chamar `localhost`: use o CLI deles
(`abacatepay listen --forward-to <url>`) ou um túnel.

## Usar sem Spring

```java
AbacatePay abacatePay = new AbacatePay(System.getenv("ABACATEPAY_API_KEY"));
Checkout checkout = abacatePay.checkouts().create(
        CreateCheckoutRequest.builder().item("prod_abc123", 1).build());
```

A verificação manual de webhooks está em `WebhookVerifier` e `WebhookParser` (pacote `webhook`).

## Testes

```bash
mvn test
```

O teste de integração com a API real é ignorado por padrão. Para rodá-lo, defina `ABACATEPAY_API_KEY`
(**somente chave `abc_dev_`**) e `ABACATEPAY_PRODUCT_ID`, como variável de ambiente ou em
`abacatepay-core/local-test.properties` (já está no `.gitignore`).

## Contribuindo

Issues e PRs são bem-vindos. O projeto segue TDD: toda mudança de comportamento começa por um teste que falha.
Rode `mvn test` antes de abrir o PR.

## Licença

[MIT](LICENSE)
