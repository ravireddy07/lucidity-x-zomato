package com.cartoffer;

import com.cartoffer.store.OfferStore;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CartOfferApplicationTests {
    static WireMockServer wireMock;

    @Autowired
    MockMvc mvc;

    @Autowired
    OfferStore store;

    /*
     * This Tests use an in-process WireMock on a random port and inject
     * segment.baseUrl dynamically.
     * We don’t need Docker/MockServer for tests.
     * If you prefer a fixed port (e.g., 1080),
     * make sure no external service is listening on that port while running tests.
     */
    @BeforeAll
    static void startWiremock() {
        // use a free random port instead of hardcoding 1080
        wireMock = new WireMockServer(options().dynamicPort());
        wireMock.start();
        configureFor("localhost", wireMock.port());
    }

    // Inject the dynamic WireMock base URL into the app under test
    @DynamicPropertySource
    static void appProps(DynamicPropertyRegistry registry) {
        registry.add("segment.baseUrl", () -> "http://localhost:" + wireMock.port());
    }

    @AfterAll
    static void stopWiremock() {
        if (wireMock != null)
            wireMock.stop();
    }

    @BeforeEach
    void reset() {
        wireMock.resetAll();
        store.clear();
    }

    @Test
    public void flat_amount_discount_p1() throws Exception {
        mvc.perform(post("/api/v1/offer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"restaurant_id\":1,\"offer_type\":\"FLATX\",\"offer_value\":10,\"customer_segment\":[\"p1\"]}"))
                .andExpect(status().isOk());

        wireMock.stubFor(get(urlPathEqualTo("/api/v1/user_segment"))
                .withQueryParam("user_id", equalTo("101"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"segment\":\"p1\"}")));

        mvc.perform(post("/api/v1/cart/apply_offer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cart_value\":200,\"user_id\":101,\"restaurant_id\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cart_value").value(190));
    }

    @Test
    public void percentage_discount_p2() throws Exception {
        mvc.perform(post("/api/v1/offer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"restaurant_id\":2,\"offer_type\":\"FLATX%\",\"offer_value\":10,\"customer_segment\":[\"p2\"]}"))
                .andExpect(status().isOk());

        wireMock.stubFor(get(urlPathEqualTo("/api/v1/user_segment"))
                .withQueryParam("user_id", equalTo("202"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"segment\":\"p2\"}")));

        mvc.perform(post("/api/v1/cart/apply_offer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cart_value\":200,\"user_id\":202,\"restaurant_id\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cart_value").value(180));
    }

    @Test
    public void no_offer_for_segment() throws Exception {
        mvc.perform(post("/api/v1/offer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"restaurant_id\":3,\"offer_type\":\"FLATX\",\"offer_value\":50,\"customer_segment\":[\"p1\"]}"))
                .andExpect(status().isOk());

        wireMock.stubFor(get(urlPathEqualTo("/api/v1/user_segment"))
                .withQueryParam("user_id", equalTo("303"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"segment\":\"p3\"}")));

        mvc.perform(post("/api/v1/cart/apply_offer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cart_value\":500,\"user_id\":303,\"restaurant_id\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cart_value").value(500));
    }

    @Test
    public void multiple_offers_best_discount() throws Exception {
        mvc.perform(post("/api/v1/offer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"restaurant_id\":4,\"offer_type\":\"FLATX\",\"offer_value\":10,\"customer_segment\":[\"p1\"]}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/offer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"restaurant_id\":4,\"offer_type\":\"FLATX%\",\"offer_value\":10,\"customer_segment\":[\"p1\"]}"))
                .andExpect(status().isOk());

        wireMock.stubFor(get(urlPathEqualTo("/api/v1/user_segment"))
                .withQueryParam("user_id", equalTo("404"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"segment\":\"p1\"}")));

        mvc.perform(post("/api/v1/cart/apply_offer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cart_value\":200,\"user_id\":404,\"restaurant_id\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cart_value").value(180));
    }

    @Test
    public void restaurant_scoping() throws Exception {
        mvc.perform(post("/api/v1/offer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"restaurant_id\":10,\"offer_type\":\"FLATX\",\"offer_value\":20,\"customer_segment\":[\"p2\"]}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/offer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"restaurant_id\":11,\"offer_type\":\"FLATX%\",\"offer_value\":10,\"customer_segment\":[\"p2\"]}"))
                .andExpect(status().isOk());

        wireMock.stubFor(get(urlPathEqualTo("/api/v1/user_segment"))
                .withQueryParam("user_id", equalTo("505"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"segment\":\"p2\"}")));

        mvc.perform(post("/api/v1/cart/apply_offer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cart_value\":300,\"user_id\":505,\"restaurant_id\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cart_value").value(280));

        mvc.perform(post("/api/v1/cart/apply_offer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cart_value\":300,\"user_id\":505,\"restaurant_id\":11}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cart_value").value(270));
    }
}
