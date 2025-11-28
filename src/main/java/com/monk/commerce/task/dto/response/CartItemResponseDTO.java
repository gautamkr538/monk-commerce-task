package com.monk.commerce.task.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponseDTO {
    
    @JsonProperty("product_id")
    private Long productId;
    
    private Integer quantity;
    
    private BigDecimal price;
    
    @JsonProperty("total_discount")
    private BigDecimal totalDiscount;
}
