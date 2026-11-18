package io.github.paccanaro18.abacatepay.spring;

import io.github.paccanaro18.abacatepay.webhook.InMemoryProcessedEventStore;
import io.github.paccanaro18.abacatepay.webhook.ProcessedEventStore;
import io.github.paccanaro18.abacatepay.webhook.WebhookEvent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AbacatePayWebhookAutoConfigurationTest {

    private static final String[] CONFIG_COMPLETA = {
            "abacatepay.api-key=abc_dev_123",
            "abacatepay.webhook.secret=meu-secret"
    };

    private final WebApplicationContextRunner webRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    AbacatePayAutoConfiguration.class,
                    AbacatePayWebhookAutoConfiguration.class));

    private final ApplicationContextRunner semWebRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    AbacatePayAutoConfiguration.class,
                    AbacatePayWebhookAutoConfiguration.class));

    @Test
    void registraOControllerEmAppWebComSecret() {
        webRunner.withPropertyValues(CONFIG_COMPLETA)
                .run(contexto -> assertThat(contexto).hasSingleBean(AbacatePayWebhookController.class));
    }

    @Test
    void naoRegistraQuandoEnabledEhFalse() {
        webRunner.withPropertyValues(CONFIG_COMPLETA)
                .withPropertyValues("abacatepay.webhook.enabled=false")
                .run(contexto -> assertThat(contexto).doesNotHaveBean(AbacatePayWebhookController.class));
    }

    @Test
    void naoRegistraSemSecret() {
        webRunner.withPropertyValues("abacatepay.api-key=abc_dev_123")
                .run(contexto -> assertThat(contexto).doesNotHaveBean(AbacatePayWebhookController.class));
    }

    @Test
    void naoRegistraForaDeUmAppWeb() {
        semWebRunner.withPropertyValues(CONFIG_COMPLETA)
                .run(contexto -> assertThat(contexto).doesNotHaveBean(AbacatePayWebhookController.class));
    }

    @Test
    void autoConfiguracaoWebEstaRegistradaNoArquivoImports() {
        List<String> candidatas = new ArrayList<>();
        ImportCandidates.load(AutoConfiguration.class, getClass().getClassLoader())
                .forEach(candidatas::add);

        assertThat(candidatas).contains(AbacatePayWebhookAutoConfiguration.class.getName());
    }

    private static final String CHAVE_PUBLICA = "chave-publica-de-teste";
    private static final String CORPO = "{\"event\":\"checkout.completed\",\"id\":\"evt_123\"}";
    private static final String ASSINATURA = "jVYDeStZ7MLk3CER9H4OYe80UoAF2/1SHFePGZtNTf0=";

    @Configuration(proxyBeanMethods = false)
    static class ComStore {
        @Bean
        ProcessedEventStore processedEventStore() {
            return new InMemoryProcessedEventStore();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class ContaEventos {
        static final List<WebhookEvent> RECEBIDOS = new ArrayList<>();

        @Bean
        Contador contador() {
            return new Contador();
        }

        static class Contador {
            @EventListener
            void aoReceber(WebhookEvent evento) {
                RECEBIDOS.add(evento);
            }
        }
    }

    @Test
    void comStoreRegistradoIgnoraAEntregaDuplicada() {
        ContaEventos.RECEBIDOS.clear();
        webRunner.withPropertyValues(CONFIG_COMPLETA)
                .withPropertyValues("abacatepay.webhook.public-key=" + CHAVE_PUBLICA)
                .withUserConfiguration(ComStore.class, ContaEventos.class)
                .run(contexto -> {
                    AbacatePayWebhookController controller = contexto.getBean(AbacatePayWebhookController.class);

                    controller.receive(CORPO.getBytes(), ASSINATURA, null, "meu-secret");
                    controller.receive(CORPO.getBytes(), ASSINATURA, null, "meu-secret");

                    assertThat(ContaEventos.RECEBIDOS).hasSize(1);
                });
    }

    @Test
    void semStoreRegistradoCadaEntregaEhPublicada() {
        ContaEventos.RECEBIDOS.clear();
        webRunner.withPropertyValues(CONFIG_COMPLETA)
                .withPropertyValues("abacatepay.webhook.public-key=" + CHAVE_PUBLICA)
                .withUserConfiguration(ContaEventos.class)
                .run(contexto -> {
                    AbacatePayWebhookController controller = contexto.getBean(AbacatePayWebhookController.class);

                    controller.receive(CORPO.getBytes(), ASSINATURA, null, "meu-secret");
                    controller.receive(CORPO.getBytes(), ASSINATURA, null, "meu-secret");

                    assertThat(ContaEventos.RECEBIDOS).hasSize(2);
                });
    }
}
