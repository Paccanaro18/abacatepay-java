package io.github.paccanaro18.abacatepay.spring;

import io.github.paccanaro18.abacatepay.exception.WebhookParseException;
import io.github.paccanaro18.abacatepay.exception.WebhookVerificationException;
import io.github.paccanaro18.abacatepay.webhook.WebhookEvent;
import io.github.paccanaro18.abacatepay.webhook.WebhookParser;
import io.github.paccanaro18.abacatepay.webhook.WebhookVerifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AbacatePayWebhookController {

    private final WebhookVerifier verifier;
    private final ApplicationEventPublisher publisher;

    public AbacatePayWebhookController(WebhookVerifier verifier, ApplicationEventPublisher publisher) {
        this.verifier = verifier;
        this.publisher = publisher;
    }

    @PostMapping("${abacatepay.webhook.path:/abacatepay/webhook}")
    public ResponseEntity<Void> receive(
            @RequestBody byte[] rawBody,
            @RequestHeader(name = "X-Webhook-Signature", required = false) String signature,
            @RequestHeader(name = "X-Webhook-Secret", required = false) String secretHeader,
            @RequestParam(name = "webhookSecret", required = false) String secretParam) {

        String secret = secretHeader != null ? secretHeader : secretParam;

        WebhookEvent event;
        try {
            verifier.verify(rawBody, signature, secret);
            event = WebhookParser.parse(rawBody);
        } catch (WebhookVerificationException | WebhookParseException e) {
            return ResponseEntity.status(e.statusCode()).build();
        }

        publisher.publishEvent(event);
        return ResponseEntity.ok().build();
    }
}