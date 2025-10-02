package com.cartoffer;

import com.cartoffer.support.BaseCartOffer;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

class CartOffer_Validation extends BaseCartOffer {

    @Disabled("Enable after adding Bean Validation for cart_value >= 0")
    @Test
    @DisplayName("11) Validation: negative cart_value → 400")
    void tc11_negative_cart_value_400() {
        given().contentType(ContentType.JSON)
                .body("{\"cart_value\":-1,\"user_id\":101,\"restaurant_id\":1}")
                .when().post("/api/v1/cart/apply_offer")
                .then().statusCode(400);
    }

    @Disabled("Enable after adding Bean Validation for required fields")
    @Test
    @DisplayName("12) Validation: missing restaurant_id → 400")
    void tc12_missing_restaurant_id_400() {
        given().contentType(ContentType.JSON)
                .body("{\"cart_value\":200,\"user_id\":101}")
                .when().post("/api/v1/cart/apply_offer")
                .then().statusCode(400);
    }

    @Disabled("Enable after adding enum validation for offer_type")
    @Test
    @DisplayName("13) Validation: invalid offer_type → 400")
    void tc13_invalid_offer_type_400() {
        given().contentType(ContentType.JSON)
                .body("{\"restaurant_id\":1,\"offer_type\":\"BOGO\",\"offer_value\":10,\"customer_segment\":[\"p1\"]}")
                .when().post("/api/v1/offer")
                .then().statusCode(400);
    }

    @Disabled("Enable after adding non-negative offer_value validation")
    @Test
    @DisplayName("14) Validation: negative offer_value → 400")
    void tc14_negative_offer_value_400() {
        given().contentType(ContentType.JSON)
                .body("{\"restaurant_id\":1,\"offer_type\":\"FLATX\",\"offer_value\":-5,\"customer_segment\":[\"p1\"]}")
                .when().post("/api/v1/offer")
                .then().statusCode(400);
    }

    @Disabled("Enable after adding non-empty customer_segment validation")
    @Test
    @DisplayName("15) Validation: empty customer_segment → 400")
    void tc15_empty_segment_400() {
        given().contentType(ContentType.JSON)
                .body("{\"restaurant_id\":1,\"offer_type\":\"FLATX\",\"offer_value\":10,\"customer_segment\":[]}")
                .when().post("/api/v1/offer")
                .then().statusCode(400);
    }
}
