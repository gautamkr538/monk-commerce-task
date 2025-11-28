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
public class ApplicableCouponResponseDTO {
    
    @JsonProperty("coupon_id")
    private Long couponId;
    
    private String type;
    
    private BigDecimal discount;
}
