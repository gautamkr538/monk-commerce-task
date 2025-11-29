package com.monk.commerce.task.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.monk.commerce.task.util.Constants;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponRequestDTO {

    // Used JsonProperty to map JSON field like: "coupon_code" to Java field "couponCode"
    // Added validation annotations to ensure data integrity

    // Coupon code must not be blank
    @JsonProperty("coupon_code")
    private String couponCode;
    // Type must not be blank
    @NotBlank(message = Constants.COUPON_TYPE_REQUIRED)
    private String type;
    // Description of the coupon
    private String description;
    // Indicates if the coupon is active
    @JsonProperty("is_active")
    private Boolean isActive;
    // Expiration date formatted as ISO 8601 date-time string
    @JsonProperty("expiration_date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expirationDate;
    // Additional details about the coupon
    private Object details;
}
