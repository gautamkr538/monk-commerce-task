package com.monk.commerce.task.service;

import com.monk.commerce.task.dto.request.CouponRequestDTO;
import com.monk.commerce.task.dto.response.CouponResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CouponService {

    CouponResponseDTO createCoupon(CouponRequestDTO request);

    List<CouponResponseDTO> getAllCoupons();

    CouponResponseDTO getCouponById(UUID id);

    CouponResponseDTO updateCoupon(UUID id, CouponRequestDTO request);

    void deleteCoupon(UUID id);
}
