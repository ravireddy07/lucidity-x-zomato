package com.cartoffer.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfferRequest {
  @NotNull
  private Integer restaurant_id;

  @NotBlank
  @Pattern(regexp = "^(FLATX|FLATX%)$")
  private String offer_type;

  @NotNull
  @Min(0)
  private Integer offer_value;

  @NotNull
  @Size(min = 1)
  private List<@Pattern(regexp = "^(p1|p2|p3)$") String> customer_segment;
}
