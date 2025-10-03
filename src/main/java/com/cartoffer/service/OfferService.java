package com.cartoffer.service;

import com.cartoffer.store.OfferStore;
import com.cartoffer.web.dto.ApplyOfferRequest;
import com.cartoffer.web.dto.OfferRequest;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
public class OfferService {
    private final OfferStore store;
    private final SegmentClient segmentClient;
    private final DiscountService discountService;

    public OfferService(OfferStore store, SegmentClient segmentClient, DiscountService discountService) {
        this.store = store;
        this.segmentClient = segmentClient;
        this.discountService = discountService;
    }

    public void seed(OfferRequest req) {
        store.addOffer(
                req.restaurant_id(),
                req.offer_type(),
                req.offer_value(),
                new HashSet<>(req.customer_segment()));
    }

    public int apply(ApplyOfferRequest req) {
        String segment = segmentClient.getSegmentForUser(req.user_id());
        if (segment == null)
            return req.cart_value();

        List<OfferStore.Offer> offers = store.getOffers(req.restaurant_id(), segment);
        if (offers.isEmpty())
            return req.cart_value();

        List<OfferRequest> offerRequests = offers.stream()
                .map(o -> new OfferRequest(o.restaurantId(), o.type(), o.value(), List.copyOf(o.segments())))
                .toList();

        return discountService.applyBest(req.cart_value(), offerRequests);
    }
}
