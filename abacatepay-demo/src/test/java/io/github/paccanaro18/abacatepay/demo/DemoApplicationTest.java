package io.github.paccanaro18.abacatepay.demo;

import io.github.paccanaro18.abacatepay.AbacatePay;
import io.github.paccanaro18.abacatepay.webhook.WebhookEvent;
import io.github.paccanaro18.abacatepay.webhook.WebhookEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "abacatepay.api-key=abc_dev_123",
        "abacatepay.webhook.secret=meu-secret",
        "abacatepay.webhook.public-key=chave-publica-de-teste"
})
@AutoConfigureMockMvc
class DemoApplicationTest {

    private static final String CORPO = "{\"event\":\"checkout.completed\",\"id\":\"evt_123\"}";
    private static final String ASSINATURA = "jVYDeStZ7MLk3CER9H4OYe80UoAF2/1SHFePGZtNTf0=";

    @TestConfiguration
    public static class Coletor {

        public final List<WebhookEvent> eventos = new CopyOnWriteArrayList<>();

        @EventListener
        public void guardar(WebhookEvent evento) {
            eventos.add(evento);
        }
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Coletor coletor;

    @Autowired
    private AbacatePay abacatePay;

    @BeforeEach
    void limpar() {
        coletor.eventos.clear();
    }

    @Test
    void starterCriaOClienteDaApi() {
        assertNotNull(abacatePay);
    }

    @Test
    void webhookAssinadoPassaPeloStarterEChegaAosListeners() throws Exception {
        mvc.perform(post("/abacatepay/webhook")
                        .param("webhookSecret", "meu-secret")
                        .header("X-Webhook-Signature", ASSINATURA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk());

        assertEquals(1, coletor.eventos.size());
        assertEquals("evt_123", coletor.eventos.get(0).id());
        assertEquals(WebhookEventType.CHECKOUT_COMPLETED, coletor.eventos.get(0).type());
    }

    @Test
    void assinaturaFalsaDevolve401SemChegarAosListeners() throws Exception {
        mvc.perform(post("/abacatepay/webhook")
                        .param("webhookSecret", "meu-secret")
                        .header("X-Webhook-Signature", "assinatura-falsa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));

        assertTrue(coletor.eventos.isEmpty());
    }
}
