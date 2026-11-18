package io.github.paccanaro18.abacatepay.demo;

import io.github.paccanaro18.abacatepay.checkouts.Checkout;
import io.github.paccanaro18.abacatepay.webhook.CheckoutEventData;
import io.github.paccanaro18.abacatepay.webhook.WebhookEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class PagamentoListener {

    private static final Logger log = LoggerFactory.getLogger(PagamentoListener.class);

    @EventListener
    void aoReceberWebhook(WebhookEvent event) {
        log.info("Webhook recebido: id={} evento={} devMode={} apiVersion={}",
                event.id(), event.event(), event.devMode(), event.apiVersion());
        log.info("data bruto: {}", event.data() == null ? "(sem data)" : event.data().toPrettyString());

        if (!event.type().isCheckout()) {
            return;
        }

        try {
            CheckoutEventData dados = event.checkoutData();
            Checkout checkout = dados.checkout();
            if (checkout == null) {
                log.warn("O payload não trouxe data.checkout");
                return;
            }
            log.info("Checkout lido: id={} externalId={} status={} amount={} paidAmount={}",
                    checkout.id(), checkout.externalId(), checkout.status(), checkout.amount(), checkout.paidAmount());
        } catch (RuntimeException e) {
            log.warn("Não consegui ler o payload como checkout: {}", e.getMessage());
        }
    }
}
