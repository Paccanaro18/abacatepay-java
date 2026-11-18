package io.github.paccanaro18.abacatepay.demo;

import io.github.paccanaro18.abacatepay.AbacatePay;
import io.github.paccanaro18.abacatepay.Page;
import io.github.paccanaro18.abacatepay.checkouts.Checkout;
import io.github.paccanaro18.abacatepay.checkouts.CreateCheckoutRequest;
import io.github.paccanaro18.abacatepay.checkouts.ListCheckoutsRequest;
import io.github.paccanaro18.abacatepay.exception.AbacatePayException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/demo")
public class DemoController {

    private final AbacatePay abacatePay;

    public DemoController(AbacatePay abacatePay) {
        this.abacatePay = abacatePay;
    }

    @GetMapping("/checkout")
    public Checkout criar(@RequestParam("productId") String productId) {
        return abacatePay.checkouts().create(
                CreateCheckoutRequest.builder()
                        .item(productId, 1)
                        .externalId("demo-" + UUID.randomUUID())
                        .build());
    }

    @GetMapping("/checkout/{id}")
    public Checkout buscar(@PathVariable("id") String id) {
        return abacatePay.checkouts().get(id);
    }

    @GetMapping("/checkouts")
    public Page<Checkout> listar() {
        return abacatePay.checkouts().list(ListCheckoutsRequest.builder().limit(5).build());
    }

    @ExceptionHandler(AbacatePayException.class)
    public ResponseEntity<Map<String, Object>> erroDaApi(AbacatePayException e) {
        int status = e.statusCode() >= 400 ? e.statusCode() : 502;
        return ResponseEntity.status(status).body(Map.of("status", e.statusCode(), "erro", e.getMessage()));
    }
}
