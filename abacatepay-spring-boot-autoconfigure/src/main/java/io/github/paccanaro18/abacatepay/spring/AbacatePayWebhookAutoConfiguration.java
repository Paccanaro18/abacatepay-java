package io.github.paccanaro18.abacatepay.spring;

import io.github.paccanaro18.abacatepay.webhook.ProcessedEventStore;
import io.github.paccanaro18.abacatepay.webhook.WebhookVerifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = AbacatePayAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
@ConditionalOnProperty(prefix = "abacatepay.webhook", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AbacatePayWebhookAutoConfiguration {

    @Bean
    @ConditionalOnBean(WebhookVerifier.class)
    @ConditionalOnMissingBean
    public AbacatePayWebhookController abacatePayWebhookController(
            WebhookVerifier verifier, ApplicationEventPublisher publisher,
            ObjectProvider<ProcessedEventStore> processedEvents) {
        return new AbacatePayWebhookController(verifier, publisher, processedEvents.getIfAvailable());
    }
}