package com.cartoffer.service;

import org.springframework.stereotype.Service;
import com.cartoffer.model.OfferRequest;
import java.util.List;

@Service
public class DiscountService {
  public int applyBest(int cartValue, List<OfferRequest> offers) {
    if (offers == null || offers.isEmpty())
      return cartValue;
    int best = cartValue;
    for (OfferRequest o : offers) {
      if ("FLATX".equals(o.getOffer_type())) {
        int v = Math.max(cartValue - o.getOffer_value(), 0);
        if (v < best)
          best = v;
      } else if ("FLATX%".equals(o.getOffer_type())) {
        int off = (int) Math.round(cartValue * (o.getOffer_value() / 100.0));
        int v = Math.max(cartValue - off, 0);
        if (v < best)
          best = v;
      }
    }
    return best;
  }
}
