package com.monk.commerce.task.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatedCartDTO {
    
    private List<CartItemResponseDTO> items;
    
    @JsonProperty("total_price")
    private BigDecimal totalPrice;
    
    @JsonProperty("total_discount")
    private BigDecimal totalDiscount;
    
    @JsonProperty("final_price")
    private BigDecimal finalPrice;
}
