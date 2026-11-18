# 🥑 abacatepay-java — NÃO OFICIAL

SDK em Java e starter para Spring Boot da [API v2 do AbacatePay](https://docs.abacatepay.com).

> ⚠️ **Projeto não oficial e independente.** Não é criado, mantido nem endossado pelo AbacatePay.
> O nome "AbacatePay" pertence aos respectivos donos. Em desenvolvimento: a API deste SDK pode mudar antes da 1.0.

## O que é

O AbacatePay é um gateway de pagamentos brasileiro focado em Pix. Ele expõe uma API HTTP, e a documentação lista SDKs oficiais
em várias linguagens. Pelo que pesquisei, os de Java e Kotlin foram feitos para a **API v1 (legado)**; confira o README deles,
porque podem ter sido atualizados. Este projeto cobre a **API v2** na JVM, com integração nativa ao Spring Boot.

Ele resolve o que todo mundo acaba reescrevendo à mão:

- montar a requisição e ler o envelope `{success, data, error}`;
- traduzir erros HTTP em exceções tipadas;
- verificar a assinatura dos webhooks (secret + HMAC-SHA256) e expor um endpoint pronto;
- evitar processar duas vezes o mesmo evento, quando o provedor reenvia.

## Módulos

| Módulo | Para que serve |
|---|---|
| `abacatepay-core` | Cliente HTTP, modelos, erros e verificação de webhook. Java puro, sem Spring. |
| `abacatepay-spring-boot-autoconfigure` | Configura tudo a partir do `application.properties` e registra o endpoint de webhook. |
| `abacatepay-spring-boot-starter` | A dependência única que você adiciona no projeto (agrega os dois acima). |

## Funcionalidades

| Disponível | Planejado |
|---|---|
| Criar, consultar e listar checkouts (com filtros e paginação por cursor) | |
| Clientes e produtos: criar, consultar, listar e excluir | |
| Assinaturas: criar checkout, consultar, listar, cancelar e excluir checkout | |
| Erros tipados (401, 400, 404, 422, 429, 5xx, conexão) | |
| Verificação de webhook (secret + HMAC-SHA256) | Checkout transparente, links de pagamento e saques |
| Endpoint de webhook que publica `ApplicationEvent` | Publicação no Maven Central |
| Enums tolerantes a valores novos da API | |
| Datas tipadas (`Instant` e `LocalDate`) | |
| Deduplicação opcional de eventos por `id` (atômica) | |

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

As datas dos modelos são tipadas: `createdAt` e `updatedAt` vêm como `Instant`, e o vencimento (`dueDate`) como `LocalDate`.
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

## Clientes e produtos

```java
Customer cliente = abacatePay.customers().create(
        CreateCustomerRequest.builder()
                .email("joao@exemplo.com")
                .name("João Silva")
                .taxId("11144477735")
                .build());

Product produto = abacatePay.products().create(
        CreateProductRequest.builder()
                .externalId("camiseta-premium")
                .name("Camiseta Premium")
                .price(8990) // em centavos
                .build());

Product plano = abacatePay.products().create(
        CreateProductRequest.builder()
                .externalId("plano-mensal")
                .name("Plano Mensal")
                .price(4990)
                .cycle(BillingCycle.MONTHLY)
                .trialDays(7)
                .build());

Customer porEmail = abacatePay.customers().getByEmail("joao@exemplo.com");
Product porExternalId = abacatePay.products().getByExternalId("camiseta-premium");
Page<Customer> clientes = abacatePay.customers().list();
```

O `id` do produto (`prod_...`) é o que vai nos itens do checkout. A exclusão (`customers().delete(id)` e
`products().delete(id)`) é irreversível: o produto excluído some das listagens e volta com status `ARCHIVED`.
Endereço e CEP seguem as regras da API (CEP com 8 dígitos, sem hífen; estado com 2 letras).

## Assinaturas

O produto precisa ter um ciclo (`BillingCycle`). O cliente assina pelo checkout, e cada pagamento
confirmado gera uma assinatura (`subs_...`).

```java
Checkout checkout = abacatePay.subscriptions().createCheckout(
        CreateSubscriptionCheckoutRequest.builder()
                .item("prod_plano_mensal", 1)
                .customerId("cust_abc123xyz")
                .externalId("assinatura-123")
                .methods(PaymentMethod.CARD)
                .retryPolicy(3, 2) // até 3 tentativas, a cada 2 dias
                .build());
// redirecione o cliente para checkout.url()

Subscription assinatura = abacatePay.subscriptions().get("subs_abc123xyz");
Subscription porCliente = abacatePay.subscriptions().getByCustomerId("cust_abc123xyz");
Page<Subscription> ativas = abacatePay.subscriptions().list(
        ListSubscriptionsRequest.builder().status(SubscriptionStatus.ACTIVE).build());

abacatePay.subscriptions().cancel("subs_abc123xyz");
```

Para boleto, use `methods(PaymentMethod.BOLETO)` com `dueDate` (hoje até 365 dias), `interest` e `fine`. Esses campos
e o `upSellProductId` não aparecem na documentação pública, então confirme o comportamento antes de usar em produção.

O checkout de assinatura aceita um único item. `deleteCheckout("bill_...")` remove o checkout, mas não cancela
as assinaturas que ele já gerou: para isso use `cancel`. Se a busca por `customerId` ou `externalId` casar com mais
de uma assinatura, a API devolve a mais recente.

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
| Corpo vazio | `401`, sem corpo |
| Assinatura válida, mas corpo inválido | `400` |
| Seu listener lança exceção | `500`: o AbacatePay **tenta de novo** |
| Evento repetido (com deduplicação ligada) | `200`, sem publicar de novo |
| Tudo certo | `200` |

> **Seu listener precisa ser idempotente.** Como falhas provocam nova tentativa, o mesmo `event.id()` pode chegar mais de uma vez.

### Deduplicação opcional

Por padrão a deduplicação vem **desligada**. Declare um bean `ProcessedEventStore` e o endpoint passa a ignorar eventos cujo `id`
já foi recebido: responde `200` sem publicar de novo.

```java
@Bean
ProcessedEventStore processedEventStore() {
    return new InMemoryProcessedEventStore();
}
```

O endpoint reivindica o `id` antes de publicar (`tryClaim`) e devolve a reivindicação (`release`) se o seu listener falhar.
Assim, entregas simultâneas do mesmo evento têm um único vencedor, e uma falha continua provocando nova tentativa.

O `InMemoryProcessedEventStore` guarda os últimos 10.000 ids na memória: some ao reiniciar e não é compartilhado entre
instâncias. Com mais de uma instância, implemente a interface com banco ou Redis. Nesse caso, `tryClaim` precisa ser
atômico (por exemplo `SET NX` no Redis ou `INSERT` com chave única).

### Segurança do endpoint

- O AbacatePay envia o secret na URL (`?webhookSecret=`), e a query string pode aparecer em logs de acesso e em proxies.
  Use HTTPS, não registre a query string e, quando o seu ambiente permitir, prefira o header `X-Webhook-Secret`.
- A chave pública usada na assinatura já vem embutida e pode ser trocada com `abacatepay.webhook.public-key`.

### Testando em desenvolvimento

O AbacatePay não consegue chamar `localhost`: use o CLI deles (`abacatepay listen --forward-to <url>`) ou um túnel.
O CLI encaminha a assinatura, mas não o secret, então ponha `?webhookSecret=...` na URL de destino (entre aspas no PowerShell).

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
