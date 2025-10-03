package com.cartoffer.service;

import com.cartoffer.web.dto.OfferRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class DiscountService {
  /**
   * Returns the best (lowest) payable cart value after applying the best offer.
   * - FLATX: flat amount off
   * - FLATX%: percentage off with HALF_UP rounding
   * - Result is clamped to >= 0
   */
  public int applyBest(int cartValue, List<OfferRequest> offers) {
    if (offers == null || offers.isEmpty())
      return cartValue;

    int best = cartValue;

    for (OfferRequest o : offers) {
      String type = o.offer_type();
      int value = o.offer_value();

      if ("FLATX".equals(type)) {
        int v = Math.max(cartValue - value, 0);
        if (v < best)
          best = v;

      } else if ("FLATX%".equals(type)) {
        // percentage with HALF_UP rounding
        BigDecimal pct = BigDecimal.valueOf(value).divide(BigDecimal.valueOf(100));
        BigDecimal raw = BigDecimal.valueOf(cartValue).multiply(pct);
        int off = raw.setScale(0, RoundingMode.HALF_UP).intValue();
        int v = Math.max(cartValue - off, 0);
        if (v < best)
          best = v;
      }
    }

    return best;
  }
}
