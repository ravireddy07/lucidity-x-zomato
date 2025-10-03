package com.cartoffer.web.dto;

import jakarta.validation.constraints.*;

public record ApplyOfferRequest(
        @NotNull @Min(0) Integer cart_value,
        @NotNull Integer user_id,
        @NotNull Integer restaurant_id) {
}
