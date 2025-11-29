package com.monk.commerce.task.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.monk.commerce.task.util.Constants;
import jakarta.validation.constraints.DecimalMin;
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

    @NotNull(message = Constants.PRODUCT_ID_REQUIRED)
    @JsonProperty("product_id")
    private Long productId;

    @NotNull(message = Constants.QUANTITY_REQUIRED)
    @Min(value = 1, message = Constants.QUANTITY_POSITIVE)
    private Integer quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = Constants.PRICE_POSITIVE)
    private BigDecimal price;
}
