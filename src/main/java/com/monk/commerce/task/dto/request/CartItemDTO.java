package com.monk.commerce.task.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.monk.commerce.task.util.Constants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {

    // Used JsonProperty to match the expected JSON field names with underscores
    // Added validation annotations to ensure data integrity

    // Product ID must not be null
    @NotNull(message = Constants.PRODUCT_ID_REQUIRED)
    @JsonProperty("product_id")
    private Long productId;
    // Quantity must be at least 1
    @NotNull(message = Constants.QUANTITY_REQUIRED)
    @Min(value = 1, message = Constants.QUANTITY_POSITIVE)
    private Integer quantity;
    // Price must be non-negative
    @NotNull(message = "Price is required")
    @Min(value = 0, message = Constants.PRICE_POSITIVE)
    private BigDecimal price;
}
