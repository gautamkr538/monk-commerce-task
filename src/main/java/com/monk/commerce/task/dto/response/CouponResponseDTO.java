package com.monk.commerce.task.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CouponResponseDTO {
    private Long id;
    private String couponCode;
    private String type;
    private String description;
    private Boolean isActive;
    private LocalDateTime expirationDate;
    private Object details;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
