package com.monk.commerce.task.controller;

import com.monk.commerce.task.dto.request.CouponRequestDTO;
import com.monk.commerce.task.dto.response.CouponResponseDTO;
import com.monk.commerce.task.service.CouponService;
import com.monk.commerce.task.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Constants.API_BASE_PATH + Constants.COUPON_PATH)
@Tag(name = "Coupon Management", description = "APIs for managing coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    /**
     * Create a new coupon
     */
    @PostMapping
    @Operation(summary = "Create a new coupon", description = "Create a new coupon (cart-wise, product-wise, or BxGy)")
    public ResponseEntity<CouponResponseDTO> createCoupon(@Valid @RequestBody CouponRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(couponService.createCoupon(request));
    }

    /**
     * Get all coupons
     */
    @GetMapping
    @Operation(summary = "Get all coupons", description = "Retrieve all available coupons")
    public ResponseEntity<List<CouponResponseDTO>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    /**
     * Get coupon by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get coupon by ID", description = "Retrieve a specific coupon by its ID")
    public ResponseEntity<CouponResponseDTO> getCouponById(@Parameter(description = "Coupon ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(couponService.getCouponById(id));
    }

    /**
     * Update coupon by ID
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update coupon", description = "Update an existing coupon by its ID")
    public ResponseEntity<CouponResponseDTO> updateCoupon(@Parameter(description = "Coupon ID", required = true)
            @PathVariable Long id, @Valid @RequestBody CouponRequestDTO request) {
        return ResponseEntity.ok(couponService.updateCoupon(id, request));
    }

    /**
     * Delete coupon by ID
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete coupon", description = "Delete a coupon by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Coupon deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Coupon not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteCoupon(@Parameter(description = "Coupon ID", required = true) @PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }
}
