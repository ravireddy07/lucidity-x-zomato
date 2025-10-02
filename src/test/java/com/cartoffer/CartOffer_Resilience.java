package com.cartoffer;

import com.cartoffer.support.BaseCartOffer;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

class CartOffer_Resilience extends BaseCartOffer {
    @Test
    @DisplayName("16) Segment 404 → treat as no segment (unchanged total)")
    void tc16_segment_404_as_no_segment() {
        seedOffer(1, "FLATX", 10, "p1");
        expectSegmentStatus("606", 404);
        applyAndAssert(200, 606, 1, 200);
    }

    @Disabled("Enable if segment-500 maps to 502 in controller advice")
    @Test
    @DisplayName("17) Segment 500 → 502 Bad Gateway")
    void tc17_segment_500_maps_502() {
        expectSegmentStatus("707", 500);
        given().contentType(ContentType.JSON)
                .body("{\"cart_value\":200,\"user_id\":707,\"restaurant_id\":1}")
                .when().post("/api/v1/cart/apply_offer")
                .then().statusCode(502);
    }

    @Disabled("Enable if timeouts map to 502; ensure HTTP client timeout configured < delay")
    @Test
    @DisplayName("18) Segment timeout → 502")
    void tc18_segment_timeout_502() {
        expectSegmentDelay("808", 3000, "p1");
        given().contentType(ContentType.JSON)
                .body("{\"cart_value\":200,\"user_id\":808,\"restaurant_id\":1}")
                .when().post("/api/v1/cart/apply_offer")
                .then().statusCode(502);
    }

    @Disabled("Enable if malformed upstream JSON maps to 502")
    @Test
    @DisplayName("19) Segment returns malformed JSON → 502")
    void tc19_segment_malformed_502() {
        expectSegmentMalformed("909");
        given().contentType(ContentType.JSON)
                .body("{\"cart_value\":200,\"user_id\":909,\"restaurant_id\":1}")
                .when().post("/api/v1/cart/apply_offer")
                .then().statusCode(502);
    }
}
