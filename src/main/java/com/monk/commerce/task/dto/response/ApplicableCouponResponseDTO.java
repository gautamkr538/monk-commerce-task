package com.monk.commerce.task.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicableCouponResponseDTO {

    @JsonProperty("coupon_id")
    private UUID couponId;

    private String type;

    private BigDecimal discount;

    @JsonProperty("is_stackable")
    private Boolean isStackable;

    private Integer priority;

    @JsonProperty("user_usage_remaining")
    private Integer userUsageRemaining;

    @JsonProperty("global_usage_remaining")
    private Long globalUsageRemaining;
}
