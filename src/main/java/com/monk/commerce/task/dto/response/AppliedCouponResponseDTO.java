package com.monk.commerce.task.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppliedCouponResponseDTO {
    
    @JsonProperty("updated_cart")
    private UpdatedCartDTO updatedCart;
}
