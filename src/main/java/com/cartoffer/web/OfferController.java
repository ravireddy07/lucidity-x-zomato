package com.cartoffer.web;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import com.cartoffer.config.AppProps;
import com.cartoffer.model.ApplyOfferRequest;
import com.cartoffer.model.ApplyOfferResponse;
import com.cartoffer.model.OfferRequest;
import com.cartoffer.model.SegmentResponse;
import com.cartoffer.service.DiscountService;
import com.cartoffer.store.OfferStore;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class OfferController {

    private final OfferStore store;
    private final DiscountService discountService;
    private final RestClient restClient;

    public OfferController(OfferStore store, DiscountService discountService, AppProps props) {
        this.store = store;
        this.discountService = discountService;
        // segment service base URL: defaults to http://localhost:1080 (see AppProps)
        this.restClient = RestClient.builder().baseUrl(props.getSegmentBaseUrl()).build();
    }

    @PostMapping("/offer")
    public Map<String, String> addOffer(@Valid @RequestBody OfferRequest req) {
        store.add(req);
        return Map.of("response_msg", "success");
    }

    @PostMapping("/cart/apply_offer")
    public ResponseEntity<?> apply(@Valid @RequestBody ApplyOfferRequest req) {
        try {
            SegmentResponse seg = restClient.get()
                    .uri(uri -> uri.path("/api/v1/user_segment")
                            .queryParam("user_id", req.getUser_id())
                            .build())
                    .retrieve()
                    .body(SegmentResponse.class);

            if (seg == null || seg.getSegment() == null) {
                return ResponseEntity.status(502).body(Map.of("error", "segment service returned no segment"));
            }

            List<OfferRequest> offers = store.find(req.getRestaurant_id(), seg.getSegment());
            int finalValue = discountService.applyBest(req.getCart_value(), offers);
            return ResponseEntity.ok(new ApplyOfferResponse(finalValue));
        } catch (Exception ex) {
            return ResponseEntity.status(502).body(Map.of("error", "segment service unavailable"));
        }
    }
}
