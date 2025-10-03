package com.cartoffer.web;

import com.cartoffer.service.OfferService;
import com.cartoffer.web.dto.ApplyOfferRequest;
import com.cartoffer.web.dto.OfferRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class OfferController {
    private final OfferService service;

    public OfferController(OfferService service) {
        this.service = service;
    }

    @PostMapping("/offer")
    public Map<String, String> addOffer(@Valid @RequestBody OfferRequest req) {
        service.seed(req);
        return Map.of("response_msg", "success");
    }

    @PostMapping("/cart/apply_offer")
    public ResponseEntity<Map<String, Integer>> applyOffer(@Valid @RequestBody ApplyOfferRequest req) {
        int total = service.apply(req);
        return ResponseEntity.ok(Map.of("cart_value", total));
    }
}
