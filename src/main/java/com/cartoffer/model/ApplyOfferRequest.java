package com.cartoffer.model;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ApplyOfferRequest {
  @NotNull
  @Min(0)
  private Integer cart_value;
  @NotNull
  private Integer user_id;
  @NotNull
  private Integer restaurant_id;
}
