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
    
    private Object details;
}
