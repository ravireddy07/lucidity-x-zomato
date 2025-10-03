package com.cartoffer;

import com.cartoffer.support.BaseCartOffer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CartOfferBusinessRulesTests extends BaseCartOffer {
    @Test
    @DisplayName("06) FLATX cannot reduce below zero (clamp to 0)")
    void tc06_flat_clamp_zero() {
        seedOffer(1, "FLATX", 300, "p1");
        expectSegment("101", "p1");
        applyAndAssert(200, 101, 1, 0);
    }

    @Test
    @DisplayName("07) Percentage rounding halves up (5% of 101≈5) → 96")
    void tc07_percent_rounding() {
        seedOffer(1, "FLATX%", 5, "p1");
        expectSegment("101", "p1");
        applyAndAssert(101, 101, 1, 96);
    }

    @Test
    @DisplayName("08) 0% discount → unchanged")
    void tc08_zero_percent() {
        seedOffer(1, "FLATX%", 0, "p1");
        expectSegment("101", "p1");
        applyAndAssert(200, 101, 1, 200);
    }

    @Test
    @DisplayName("09) 100% discount → zero")
    void tc09_hundred_percent() {
        seedOffer(1, "FLATX%", 100, "p1");
        expectSegment("101", "p1");
        applyAndAssert(200, 101, 1, 0);
    }

    @Test
    @DisplayName("10) Multi-segment offer (p1,p2) matches p2")
    void tc10_multi_segment_match() {
        seedOffer(1, "FLATX", 10, "p1", "p2");
        expectSegment("202", "p2");
        applyAndAssert(200, 202, 1, 190);
    }

    @Test
    @DisplayName("20) Segment case-insensitivity (P1 → p1)")
    void tc20_segment_case_insensitive() {
        seedOffer(1, "FLATX", 10, "p1");
        expectSegment("101", "P1");
        applyAndAssert(200, 101, 1, 190);
    }

    @Test
    @DisplayName("21) Idempotent seeding (same offer twice → single effect)")
    void tc21_idempotent_seed() {
        seedOffer(1, "FLATX", 10, "p1");
        seedOffer(1, "FLATX", 10, "p1");
        expectSegment("101", "p1");
        applyAndAssert(200, 101, 1, 190);
    }

    @Test
    @DisplayName("22) No offers seeded → unchanged total")
    void tc22_no_offers_seeded() {
        expectSegment("101", "p1");
        applyAndAssert(200, 101, 1, 200);
    }
}
