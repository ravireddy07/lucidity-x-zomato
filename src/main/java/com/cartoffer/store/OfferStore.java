package com.cartoffer.store;

import org.springframework.stereotype.Component;
import com.cartoffer.model.OfferRequest;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
public class OfferStore {
  private final List<OfferRequest> offers = new CopyOnWriteArrayList<>();

  public void add(OfferRequest o) {
    offers.add(o);
  }

  public void clear() {
    offers.clear();
  }

  public List<OfferRequest> find(int restaurantId, String segment) {
    return offers.stream()
        .filter(o -> o.getRestaurant_id() == restaurantId && o.getCustomer_segment().contains(segment))
        .collect(Collectors.toList());
  }
}
