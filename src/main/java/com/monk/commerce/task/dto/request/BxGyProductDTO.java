package com.monk.commerce.task.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.monk.commerce.task.util.Constants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BxGyProductDTO {

    @NotNull(message = Constants.PRODUCT_ID_REQUIRED)
    @JsonProperty("product_id")
    private Long productId;

    @NotNull(message = Constants.QUANTITY_REQUIRED)
    @Min(value = 1, message = Constants.QUANTITY_POSITIVE)
    private Integer quantity;

    @Min(value = 1, message = "Tier level must be at least 1")
    @JsonProperty("tier_level")
    private Integer tierLevel;
}
