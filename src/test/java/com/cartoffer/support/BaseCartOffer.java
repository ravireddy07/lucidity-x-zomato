package com.cartoffer.support;

import com.cartoffer.CartOfferApplication;
import com.cartoffer.store.OfferStore;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockserver.client.MockServerClient;
import org.mockserver.matchers.Times;
import org.mockserver.model.Delay;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = CartOfferApplication.class)
@ActiveProfiles("test")
public abstract class BaseCartOffer {

    protected MockServerClient mock;

    @Autowired
    protected OfferStore store;

    // ---------- Test bootstrap ----------

    @BeforeAll
    static void restAssuredSetup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 9001; // app listens here
    }

    @BeforeEach
    void resetState() {
        // clear in-memory offers
        store.clear();
        // connect & reset expectations on MockServer (:1080)
        mock = new MockServerClient("localhost", 1080);
        mock.reset();
    }

    // ---------- Helpers used by all test classes ----------

    /** Seed an offer into the app's in-memory store via API. */
    protected void seedOffer(int restaurantId, String offerType, int offerValue, String... segments) {
        // build ["p1","p2",...]
        StringBuilder segJson = new StringBuilder("[");
        for (int i = 0; i < segments.length; i++) {
            if (i > 0)
                segJson.append(',');
            segJson.append('"').append(segments[i]).append('"');
        }
        segJson.append(']');

        given()
                .contentType("application/json")
                .body("{\"restaurant_id\":" + restaurantId +
                        ",\"offer_type\":\"" + offerType +
                        "\",\"offer_value\":" + offerValue +
                        ",\"customer_segment\":" + segJson + "}")
                .when()
                .post("/api/v1/offer")
                .then()
                .statusCode(200)
                .body("response_msg", equalTo("success"));
    }

    /** Expect a normal segment (200, JSON) for the given user. */
    protected void expectSegment(String userId, String segment) {
        mock.when(
                HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/api/v1/user_segment")
                        .withQueryStringParameter("user_id", userId),
                Times.unlimited())
                .respond(
                        HttpResponse.response()
                                .withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"segment\":\"" + segment + "\"}"));
    }

    /**
     * Expect a specific HTTP status from the segment service for the given user.
     */
    protected void expectSegmentStatus(String userId, int statusCode) {
        mock.when(
                HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/api/v1/user_segment")
                        .withQueryStringParameter("user_id", userId),
                Times.unlimited())
                .respond(
                        HttpResponse.response()
                                .withStatusCode(statusCode));
    }

    /**
     * Expect the segment response to be delayed by N millis (used to trigger
     * timeouts).
     */
    protected void expectSegmentDelay(String userId, int millis, String segment) {
        mock.when(
                HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/api/v1/user_segment")
                        .withQueryStringParameter("user_id", userId),
                Times.unlimited())
                .respond(
                        HttpResponse.response()
                                .withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"segment\":\"" + segment + "\"}")
                                .withDelay(new Delay(TimeUnit.MILLISECONDS, millis)));
    }

    /** Expect a malformed body (e.g., not JSON) to test parser/handler behavior. */
    protected void expectSegmentMalformed(String userId) {
        mock.when(
                HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/api/v1/user_segment")
                        .withQueryStringParameter("user_id", userId),
                Times.unlimited())
                .respond(
                        HttpResponse.response()
                                .withStatusCode(200)
                                .withHeader("Content-Type", "text/plain")
                                .withBody("not json"));
    }

    /** Apply offer and assert the final cart total. */
    protected void applyAndAssert(int cartValue, int userId, int restaurantId, int expectedCart) {
        given()
                .contentType("application/json")
                .body("{\"cart_value\":" + cartValue +
                        ",\"user_id\":" + userId +
                        ",\"restaurant_id\":" + restaurantId + "}")
                .when()
                .post("/api/v1/cart/apply_offer")
                .then()
                .statusCode(200)
                .body("cart_value", equalTo(expectedCart));
    }
}
