package com.monk.commerce.task.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.monk.commerce.task.util.Constants;
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
public class ProductWiseDetailsDTO {
    
    @NotNull(message = Constants.PRODUCT_ID_REQUIRED)
    @JsonProperty("product_id")
    private Long productId;
    
    @DecimalMin(value = "0.0", message = "Discount must be at least 0")
    @DecimalMax(value = "100.0", message = "Discount percentage cannot exceed 100")
    @JsonProperty("discount")
    private BigDecimal discount;
}
