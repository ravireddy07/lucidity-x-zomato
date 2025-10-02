package com.cartoffer.support;

import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockserver.client.MockServerClient;
import static org.mockserver.model.Delay.milliseconds;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.port;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public abstract class BaseCartOffer {

    protected MockServerClient mock;

    @Autowired(required = false)
    protected com.cartoffer.store.OfferStore store;

    @BeforeAll
    static void raSetup() {
        baseURI = "http://localhost";
        port = 9001; // app port
    }

    @BeforeEach
    void reset() {
        mock = new MockServerClient("localhost", 1080); // MockServer port
        mock.reset();
        if (store != null)
            store.clear();
    }

    // ---- Seed offer(s) into the app ----
    protected void seedOffer(int restaurantId, String offerType, int offerValue, String... segments) {
        StringBuilder arr = new StringBuilder("[");
        for (int i = 0; i < segments.length; i++) {
            if (i > 0)
                arr.append(",");
            arr.append("\"").append(segments[i]).append("\"");
        }
        arr.append("]");

        given()
                .contentType(ContentType.JSON)
                .body("{\"restaurant_id\":" + restaurantId + ",\"offer_type\":\"" + offerType + "\",\"offer_value\":"
                        + offerValue + ",\"customer_segment\":" + arr + "}")
                .when()
                .post("/api/v1/offer")
                .then()
                .statusCode(200)
                .body("response_msg", equalTo("success"));
    }

    // ---- Expectation helpers on MockServer ----
    protected void expectSegment(String userId, String segment) {
        mock.when(HttpRequest.request()
                .withMethod("GET")
                .withPath("/api/v1/user_segment")
                .withQueryStringParameter("user_id", userId))
                .respond(HttpResponse.response()
                        .withStatusCode(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"segment\":\"" + segment + "\"}"));
    }

    protected void expectSegmentStatus(String userId, int statusCode) {
        mock.when(HttpRequest.request()
                .withMethod("GET")
                .withPath("/api/v1/user_segment")
                .withQueryStringParameter("user_id", userId))
                .respond(HttpResponse.response().withStatusCode(statusCode));
    }

    protected void expectSegmentMalformed(String userId) {
        mock.when(HttpRequest.request()
                .withMethod("GET")
                .withPath("/api/v1/user_segment")
                .withQueryStringParameter("user_id", userId))
                .respond(HttpResponse.response()
                        .withStatusCode(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{bad json"));
    }

    protected void expectSegmentDelay(String userId, long delayMs, String segment) {
        mock.when(HttpRequest.request()
                .withMethod("GET")
                .withPath("/api/v1/user_segment")
                .withQueryStringParameter("user_id", userId))
                .respond(HttpResponse.response()
                        .withStatusCode(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"segment\":\"" + segment + "\"}")
                        .withDelay(milliseconds(delayMs)));
    }

    // ---- Assert apply_offer result ----
    protected void applyAndAssert(int cartValue, int userId, int restaurantId, int expected) {
        given()
                .contentType(ContentType.JSON)
                .body("{\"cart_value\":" + cartValue + ",\"user_id\":" + userId + ",\"restaurant_id\":" + restaurantId
                        + "}")
                .when()
                .post("/api/v1/cart/apply_offer")
                .then()
                .statusCode(200)
                .body("cart_value", equalTo(expected));
    }
}
