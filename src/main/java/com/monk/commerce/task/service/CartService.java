package com.monk.commerce.task.service;

import com.monk.commerce.task.dto.request.CartRequestDTO;
import com.monk.commerce.task.dto.response.ApplicableCouponResponseDTO;
import com.monk.commerce.task.dto.response.AppliedCouponResponseDTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CartService {

    Map<String, List<ApplicableCouponResponseDTO>> getApplicableCoupons(Map<String, CartRequestDTO> request);

    AppliedCouponResponseDTO applyCoupon(UUID couponId, Map<String, CartRequestDTO> request);
}
