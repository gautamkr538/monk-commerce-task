package com.monk.commerce.task.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.monk.commerce.task.util.Constants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponRequestDTO {

    @JsonProperty("coupon_code")
    private String couponCode;

    @NotBlank(message = Constants.COUPON_TYPE_REQUIRED)
    private String type;

    private String description;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("expiration_date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expirationDate;

    @JsonProperty("max_usage_limit")
    @Min(value = 1, message = "Max usage limit must be at least 1")
    private Long maxUsageLimit;

    @JsonProperty("usage_limit_per_user")
    @Min(value = 1, message = "Usage limit per user must be at least 1")
    private Integer usageLimitPerUser;

    @JsonProperty("allow_stacking")
    private Boolean allowStacking;

    @Min(value = 0, message = "Priority cannot be negative")
    private Integer priority;

    private Object details;

    @JsonProperty("excluded_products")
    private List<Long> excludedProducts;
}
