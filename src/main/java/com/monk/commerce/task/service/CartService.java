package com.monk.commerce.task.service;


import com.monk.commerce.task.dto.request.CartRequestDTO;
import com.monk.commerce.task.dto.response.ApplicableCouponResponseDTO;
import com.monk.commerce.task.dto.response.AppliedCouponResponseDTO;

import java.util.List;

public interface CartService {

    /**
     * Get all applicable coupons for a cart
     */
    List<ApplicableCouponResponseDTO> getApplicableCoupons(CartRequestDTO cart);

    /**
     * Apply a specific coupon to a cart
     */
    AppliedCouponResponseDTO applyCoupon(Long couponId, CartRequestDTO cart);
}
