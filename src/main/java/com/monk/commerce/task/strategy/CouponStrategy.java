package com.monk.commerce.task.strategy;

import com.monk.commerce.task.dto.request.CartRequestDTO;
import com.monk.commerce.task.dto.response.AppliedCouponResponseDTO;
import com.monk.commerce.task.entity.Coupon;

import java.math.BigDecimal;

public interface CouponStrategy {

    boolean isApplicable(Coupon coupon, CartRequestDTO cart);

    BigDecimal calculateDiscount(Coupon coupon, CartRequestDTO cart);

    AppliedCouponResponseDTO applyCoupon(Coupon coupon, CartRequestDTO cart);
}
