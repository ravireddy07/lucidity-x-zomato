package com.cartoffer.web.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record OfferRequest(
		@NotNull Integer restaurant_id,
		@NotBlank @Pattern(regexp = "FLATX|FLATX%") String offer_type,
		@NotNull @Min(0) Integer offer_value,
		@NotNull @Size(min = 1) List<@NotBlank String> customer_segment) {
}
