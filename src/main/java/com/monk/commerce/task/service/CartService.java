package com.monk.commerce.task.service;


import com.monk.commerce.task.dto.request.CartRequestDTO;
import com.monk.commerce.task.dto.response.ApplicableCouponResponseDTO;
import com.monk.commerce.task.dto.response.AppliedCouponResponseDTO;

import java.util.List;
import java.util.Map;

public interface CartService {

    /**
     * Get all applicable coupons for a cart
     */
    Map<String, List<ApplicableCouponResponseDTO>> getApplicableCoupons(Map<String, CartRequestDTO> request);

    /**
     * Apply a specific coupon to a cart
     */
    AppliedCouponResponseDTO applyCoupon(Long couponId, Map<String, CartRequestDTO> request);
}
