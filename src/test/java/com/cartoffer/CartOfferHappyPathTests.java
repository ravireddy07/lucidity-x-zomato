package com.cartoffer;

import com.cartoffer.support.BaseCartOffer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CartOfferHappyPathTests extends BaseCartOffer {
    @Test
    @DisplayName("01) FLATX for p1: 200 -> 190")
    void tc01_flat_p1() {
        seedOffer(1, "FLATX", 10, "p1");
        expectSegment("101", "p1");
        applyAndAssert(200, 101, 1, 190);
    }

    @Test
    @DisplayName("02) FLATX% for p2: 200 -> 180")
    void tc02_percent_p2() {
        seedOffer(2, "FLATX%", 10, "p2");
        expectSegment("202", "p2");
        applyAndAssert(200, 202, 2, 180);
    }

    @Test
    @DisplayName("03) No matching offer → unchanged")
    void tc03_no_offer_for_segment() {
        seedOffer(3, "FLATX", 50, "p1");
        expectSegment("303", "p3");
        applyAndAssert(500, 303, 3, 500);
    }

    @Test
    @DisplayName("04) Best discount wins (FLATX vs FLATX%)")
    void tc04_best_discount() {
        seedOffer(4, "FLATX", 10, "p1");
        seedOffer(4, "FLATX%", 10, "p1");
        expectSegment("404", "p1");
        applyAndAssert(200, 404, 4, 180);
    }

    @Test
    @DisplayName("05) Restaurant scoping respected")
    void tc05_restaurant_scoping() {
        seedOffer(10, "FLATX", 20, "p2");
        seedOffer(11, "FLATX%", 10, "p2");
        expectSegment("505", "p2");
        applyAndAssert(300, 505, 10, 280);
        applyAndAssert(300, 505, 11, 270);
    }
}
