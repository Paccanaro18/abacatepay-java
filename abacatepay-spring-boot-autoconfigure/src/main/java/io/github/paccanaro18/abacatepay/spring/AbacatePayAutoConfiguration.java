package io.github.paccanaro18.abacatepay.spring;

import io.github.paccanaro18.abacatepay.AbacatePay;
import io.github.paccanaro18.abacatepay.webhook.WebhookVerifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(AbacatePay.class)
@EnableConfigurationProperties(AbacatePayProperties.class)
public class AbacatePayAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "abacatepay", name = "api-key")
    public AbacatePay abacatePay(AbacatePayProperties properties) {
        return new AbacatePay(properties.apiKey(), properties.baseUrl());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "abacatepay.webhook", name = "secret")
    public WebhookVerifier webhookVerifier(AbacatePayProperties properties) {
        return new WebhookVerifier(
                properties.webhook().publicKey(),
                properties.webhook().secret());
    }
}