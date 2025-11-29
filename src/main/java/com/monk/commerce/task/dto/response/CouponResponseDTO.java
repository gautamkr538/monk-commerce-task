package com.monk.commerce.task.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CouponResponseDTO {

    private UUID id;

    @JsonProperty("coupon_code")
    private String couponCode;

    private String type;

    private String description;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("expiration_date")
    private LocalDateTime expirationDate;

    @JsonProperty("usage_count")
    private Long usageCount;

    @JsonProperty("max_usage_limit")
    private Long maxUsageLimit;

    @JsonProperty("usage_limit_per_user")
    private Integer usageLimitPerUser;

    @JsonProperty("allow_stacking")
    private Boolean allowStacking;

    private Integer priority;

    private Object details;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}