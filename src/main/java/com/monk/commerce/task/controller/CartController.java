package com.monk.commerce.task.controller;

import com.monk.commerce.task.dto.request.CartRequestDTO;
import com.monk.commerce.task.dto.response.ApplicableCouponResponseDTO;
import com.monk.commerce.task.dto.response.AppliedCouponResponseDTO;
import com.monk.commerce.task.service.CartService;
import com.monk.commerce.task.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(Constants.API_BASE_PATH + Constants.CART_PATH)
@Tag(name = "Cart Management", description = "APIs for cart and coupon application")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/applicable-coupons")
    @Operation(summary = "Get applicable coupons", description = "Fetch all applicable coupons for a given cart")
    public ResponseEntity<Map<String, List<ApplicableCouponResponseDTO>>> getApplicableCoupons(@Valid @RequestBody Map<String, CartRequestDTO> request) {
        return ResponseEntity.ok(cartService.getApplicableCoupons(request));
    }

    @PostMapping("/apply-coupon/{id}")
    @Operation(summary = "Apply coupon to cart", description = "Apply a specific coupon to the cart")
    public ResponseEntity<AppliedCouponResponseDTO> applyCoupon(
            @Parameter(description = "Coupon ID", required = true) @PathVariable UUID id, @Valid @RequestBody Map<String, CartRequestDTO> request) {
        return ResponseEntity.ok(cartService.applyCoupon(id, request));
    }
}
