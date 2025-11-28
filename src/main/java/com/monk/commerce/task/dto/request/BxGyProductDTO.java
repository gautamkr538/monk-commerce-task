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

    // Used JsonProperty to match the expected JSON field names with underscores
    // Added validation annotations to ensure data integrity
    // Product ID must not be null
    // Quantity must be at least 1

    @NotNull(message = Constants.PRODUCT_ID_REQUIRED)
    @JsonProperty("product_id")
    private Long productId;
    
    @NotNull(message = Constants.QUANTITY_REQUIRED)
    @Min(value = 1, message = Constants.QUANTITY_POSITIVE)
    private Integer quantity;
}
