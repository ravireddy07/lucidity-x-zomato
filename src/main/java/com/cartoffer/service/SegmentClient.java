package com.cartoffer.service;

import com.cartoffer.web.UpstreamUnavailableException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.time.Duration;
import java.util.Map;

@Component
public class SegmentClient {
    private final RestTemplate rest;

    public SegmentClient(RestTemplateBuilder builder) {
        this.rest = builder
                .setConnectTimeout(Duration.ofSeconds(1))
                .setReadTimeout(Duration.ofSeconds(1))
                .build();
    }

    /**
     * @return lower-cased segment (e.g: "p1"), or null if upstream says 404 (no
     *         segment).
     *         Throws UpstreamUnavailableException for 5xx/timeout.
     */
    public String getSegmentForUser(int userId) {
        String url = "http://localhost:1080/api/v1/user_segment?user_id={userId}";
        try {
            ResponseEntity<Map> resp = rest.exchange(url, HttpMethod.GET, null, Map.class, Map.of("userId", userId));
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Object seg = resp.getBody().get("segment");
                if (seg == null)
                    return null;
                return String.valueOf(seg).toLowerCase();
            }
            if (resp.getStatusCode().value() == 404) {
                return null; // "no segment"
            }
            throw new UpstreamUnavailableException("status=" + resp.getStatusCode());
        } catch (HttpClientErrorException.NotFound e) {
            return null; // 404 -> no segment
        } catch (HttpServerErrorException e) {
            throw new UpstreamUnavailableException("5xx from segment", e);
        } catch (ResourceAccessException e) { // timeouts, IO
            throw new UpstreamUnavailableException("timeout/IO", e);
        } catch (RestClientException e) { // includes JSON mapping errors
            throw new UpstreamUnavailableException("malformed/unknown", e);
        }
    }
}
