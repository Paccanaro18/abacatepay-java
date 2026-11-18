package io.github.paccanaro18.abacatepay.spring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

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
}
