package io.github.paccanaro18.abacatepay.spring;

import io.github.paccanaro18.abacatepay.webhook.InMemoryProcessedEventStore;
import io.github.paccanaro18.abacatepay.webhook.ProcessedEventStore;
import io.github.paccanaro18.abacatepay.webhook.WebhookEvent;
import io.github.paccanaro18.abacatepay.webhook.WebhookEventType;
import io.github.paccanaro18.abacatepay.webhook.WebhookVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AbacatePayWebhookControllerTest {

    private static final String CHAVE = "chave-publica-de-teste";
    private static final String SECRET = "meu-secret";

    private static final String CORPO = "{\"event\":\"checkout.completed\",\"id\":\"evt_123\"}";
    private static final String ASSINATURA = "jVYDeStZ7MLk3CER9H4OYe80UoAF2/1SHFePGZtNTf0=";

    private static final String CORPO_SEM_EVENT = "{}";
    private static final String ASSINATURA_SEM_EVENT = "5wV4q2MQ+JDvlqIhx+hXfI/K7EtNr6dr0trvVFwXsqw=";

    private final List<Object> publicados = new ArrayList<>();
    private MockMvc mvc;

    @BeforeEach
    void montarMvc() {
        mvc = montarMvc(publicados::add);
    }

    private MockMvc montarMvc(ApplicationEventPublisher publisher) {
        AbacatePayWebhookController controller =
                new AbacatePayWebhookController(new WebhookVerifier(CHAVE, SECRET), publisher);
        return MockMvcBuilders.standaloneSetup(controller)
                .addPlaceholderValue("abacatepay.webhook.path", "/abacatepay/webhook")
                .build();
    }

    private MockHttpServletRequestBuilder requisicao(String corpo) {
        return post("/abacatepay/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void webhookValidoPublicaOEventoEDevolve200() throws Exception {
        mvc.perform(requisicao(CORPO)
                        .param("webhookSecret", SECRET)
                        .header("X-Webhook-Signature", ASSINATURA))
                .andExpect(status().isOk());

        assertThat(publicados).hasSize(1);
        WebhookEvent evento = (WebhookEvent) publicados.get(0);
        assertThat(evento.id()).isEqualTo("evt_123");
        assertThat(evento.type()).isEqualTo(WebhookEventType.CHECKOUT_COMPLETED);
    }

    @Test
    void secretPeloHeaderTambemEhAceito() throws Exception {
        mvc.perform(requisicao(CORPO)
                        .header("X-Webhook-Secret", SECRET)
                        .header("X-Webhook-Signature", ASSINATURA))
                .andExpect(status().isOk());

        assertThat(publicados).hasSize(1);
    }

    @Test
    void secretErradoDevolve401SemCorpoESemPublicar() throws Exception {
        mvc.perform(requisicao(CORPO)
                        .param("webhookSecret", "errado")
                        .header("X-Webhook-Signature", ASSINATURA))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));

        assertThat(publicados).isEmpty();
    }

    @Test
    void assinaturaInvalidaDevolve401SemCorpoESemPublicar() throws Exception {
        mvc.perform(requisicao(CORPO)
                        .param("webhookSecret", SECRET)
                        .header("X-Webhook-Signature", "assinatura-falsa"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));

        assertThat(publicados).isEmpty();
    }

    @Test
    void semSecretNemAssinaturaDevolve401() throws Exception {
        mvc.perform(requisicao(CORPO))
                .andExpect(status().isUnauthorized());

        assertThat(publicados).isEmpty();
    }

    @Test
    void corpoAssinadoMasSemEventDevolve400() throws Exception {
        mvc.perform(requisicao(CORPO_SEM_EVENT)
                        .param("webhookSecret", SECRET)
                        .header("X-Webhook-Signature", ASSINATURA_SEM_EVENT))
                .andExpect(status().isBadRequest());

        assertThat(publicados).isEmpty();
    }

    @Test
    void falhaNoListenerNaoEEngolida() {
        MockMvc comFalha = montarMvc(evento -> {
            throw new IllegalStateException("falha no listener");
        });

        Throwable erro = catchThrowable(() -> comFalha.perform(requisicao(CORPO)
                .param("webhookSecret", SECRET)
                .header("X-Webhook-Signature", ASSINATURA)));

        assertThat(erro).hasRootCauseInstanceOf(IllegalStateException.class);
    }

    private MockMvc montarMvcComStore(ApplicationEventPublisher publisher, ProcessedEventStore store) {
        AbacatePayWebhookController controller =
                new AbacatePayWebhookController(new WebhookVerifier(CHAVE, SECRET), publisher, store);
        return MockMvcBuilders.standaloneSetup(controller)
                .addPlaceholderValue("abacatepay.webhook.path", "/abacatepay/webhook")
                .build();
    }

    private MockHttpServletRequestBuilder requisicaoValida() {
        return requisicao(CORPO)
                .param("webhookSecret", SECRET)
                .header("X-Webhook-Signature", ASSINATURA);
    }

    @Test
    void semStoreOMesmoEventoEhPublicadoSempre() throws Exception {
        mvc.perform(requisicaoValida()).andExpect(status().isOk());
        mvc.perform(requisicaoValida()).andExpect(status().isOk());

        assertThat(publicados).hasSize(2);
    }

    @Test
    void eventoRepetidoNaoEhPublicadoDeNovoMasDevolve200() throws Exception {
        MockMvc comStore = montarMvcComStore(publicados::add, new InMemoryProcessedEventStore());

        comStore.perform(requisicaoValida()).andExpect(status().isOk());
        comStore.perform(requisicaoValida()).andExpect(status().isOk());

        assertThat(publicados).hasSize(1);
    }

    @Test
    void falhaNoListenerNaoMarcaOEventoComoProcessado() {
        ProcessedEventStore store = new InMemoryProcessedEventStore();
        MockMvc comFalha = montarMvcComStore(evento -> {
            throw new IllegalStateException("falha no listener");
        }, store);

        catchThrowable(() -> comFalha.perform(requisicaoValida()));

        assertThat(store.alreadyProcessed("evt_123")).isFalse();
    }

    @Test
    void semCorpoDevolve401SemCorpoDoSpring() throws Exception {
        mvc.perform(post("/abacatepay/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("webhookSecret", SECRET)
                        .header("X-Webhook-Signature", ASSINATURA))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));

        assertThat(publicados).isEmpty();
    }
}
