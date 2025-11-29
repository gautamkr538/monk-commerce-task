package com.monk.commerce.task.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
public class CartWiseDetailsDTO {

    @NotNull(message = "Threshold is required")
    @DecimalMin(value = "0.0", message = "Threshold must be non-negative")
    private BigDecimal threshold;

    @NotNull(message = "Discount percentage is required")
    @DecimalMin(value = "0.0", message = "Discount percentage must be at least 0")
    @DecimalMax(value = "100.0", message = "Discount percentage cannot exceed 100")
    @JsonProperty("discount")
    private BigDecimal discountPercentage;

    @DecimalMin(value = "0.0", message = "Maximum discount must be non-negative")
    @JsonProperty("max_discount")
    private BigDecimal maxDiscount;
}
