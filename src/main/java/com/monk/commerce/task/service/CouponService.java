package com.monk.commerce.task.service;


import com.monk.commerce.task.dto.request.CouponRequestDTO;
import com.monk.commerce.task.dto.response.CouponResponseDTO;

import java.util.List;

public interface CouponService {

    /**
     * Create a new coupon
     */
    CouponResponseDTO createCoupon(CouponRequestDTO request);

    /**
     * Get all coupons
     */
    List<CouponResponseDTO> getAllCoupons();

    /**
     * Get coupon by ID
     */
    CouponResponseDTO getCouponById(Long id);

    /**
     * Update coupon by ID
     */
    CouponResponseDTO updateCoupon(Long id, CouponRequestDTO request);

    /**
     * Delete coupon by ID
     */
    void deleteCoupon(Long id);
}
