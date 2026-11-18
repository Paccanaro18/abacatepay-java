package io.github.paccanaro18.abacatepay.spring;

import io.github.paccanaro18.abacatepay.AbacatePay;
import io.github.paccanaro18.abacatepay.webhook.AbacatePayWebhooks;
import io.github.paccanaro18.abacatepay.webhook.WebhookVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AbacatePayAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AbacatePayAutoConfiguration.class));

    @Configuration(proxyBeanMethods = false)
    static class ConfiguracaoDoUsuario {
        static final AbacatePay MEU_CLIENTE = new AbacatePay("abc_dev_do_usuario");

        @Bean
        AbacatePay meuCliente() {
            return MEU_CLIENTE;
        }
    }

    @Test
    void criaClienteEVerificadorQuandoHaConfiguracaoCompleta() {
        runner.withPropertyValues(
                        "abacatepay.api-key=abc_dev_123",
                        "abacatepay.webhook.secret=meu-secret")
                .run(contexto -> {
                    assertThat(contexto).hasSingleBean(AbacatePay.class);
                    assertThat(contexto).hasSingleBean(WebhookVerifier.class);
                });
    }

    @Test
    void naoCriaClienteSemApiKey() {
        runner.run(contexto -> assertThat(contexto).doesNotHaveBean(AbacatePay.class));
    }

    @Test
    void naoCriaVerificadorSemSecret() {
        runner.withPropertyValues("abacatepay.api-key=abc_dev_123")
                .run(contexto -> assertThat(contexto).doesNotHaveBean(WebhookVerifier.class));
    }

    @Test
    void respeitaOClienteDefinidoPeloUsuario() {
        runner.withUserConfiguration(ConfiguracaoDoUsuario.class)
                .withPropertyValues("abacatepay.api-key=abc_dev_123")
                .run(contexto -> {
                    assertThat(contexto).hasSingleBean(AbacatePay.class);
                    assertThat(contexto.getBean(AbacatePay.class))
                            .isSameAs(ConfiguracaoDoUsuario.MEU_CLIENTE);
                });
    }

    @Test
    void aplicaValoresPadrao() {
        runner.withPropertyValues("abacatepay.api-key=abc_dev_123")
                .run(contexto -> {
                    AbacatePayProperties props = contexto.getBean(AbacatePayProperties.class);

                    assertThat(props.baseUrl()).isEqualTo(AbacatePay.DEFAULT_BASE_URL);
                    assertThat(props.webhook().enabled()).isTrue();
                    assertThat(props.webhook().path()).isEqualTo("/abacatepay/webhook");
                    assertThat(props.webhook().publicKey()).isEqualTo(AbacatePayWebhooks.PUBLIC_KEY);
                    assertThat(props.webhook().secret()).isNull();
                });
    }

    @Test
    void propriedadesDoUsuarioSobrescrevemOsPadroes() {
        runner.withPropertyValues(
                        "abacatepay.api-key=abc_dev_123",
                        "abacatepay.base-url=http://localhost:9999",
                        "abacatepay.webhook.enabled=false",
                        "abacatepay.webhook.path=/meu/webhook")
                .run(contexto -> {
                    AbacatePayProperties props = contexto.getBean(AbacatePayProperties.class);

                    assertThat(props.baseUrl()).isEqualTo("http://localhost:9999");
                    assertThat(props.webhook().enabled()).isFalse();
                    assertThat(props.webhook().path()).isEqualTo("/meu/webhook");
                });
    }

    @Test
    void autoConfiguracaoEstaRegistradaNoArquivoImports() {
        List<String> candidatas = new ArrayList<>();
        ImportCandidates.load(AutoConfiguration.class, getClass().getClassLoader())
                .forEach(candidatas::add);

        assertThat(candidatas).contains(AbacatePayAutoConfiguration.class.getName());
    }
}