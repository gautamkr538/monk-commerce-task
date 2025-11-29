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

    // List of items in the cart
    // Each item includes product ID, quantity, price, and total discount
    private List<CartItemResponseDTO> items;
    // Total price of all items before discounts
    @JsonProperty("total_price")
    private BigDecimal totalPrice;
    // Total discount applied to the cart
    @JsonProperty("total_discount")
    private BigDecimal totalDiscount;
    // Final price after applying discounts
    @JsonProperty("final_price")
    private BigDecimal finalPrice;
}
