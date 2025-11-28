package com.monk.commerce.task.strategy;


import com.monk.commerce.task.dto.request.CartRequestDTO;
import com.monk.commerce.task.dto.response.AppliedCouponResponseDTO;
import com.monk.commerce.task.entity.Coupon;

import java.math.BigDecimal;

public interface CouponStrategy {

    /**
     * Check if coupon is applicable to the cart
     */
    boolean isApplicable(Coupon coupon, CartRequestDTO cart);

    /**
     * Calculate discount for the cart
     */
    BigDecimal calculateDiscount(Coupon coupon, CartRequestDTO cart);

    /**
     * Apply coupon to cart and return updated cart
     */
    AppliedCouponResponseDTO applyCoupon(Coupon coupon, CartRequestDTO cart);
}
