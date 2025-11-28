package com.monk.commerce.task.strategy;

import com.monk.commerce.task.dto.request.CartRequestDTO;
import com.monk.commerce.task.dto.response.AppliedCouponResponseDTO;
import com.monk.commerce.task.dto.response.CartItemResponseDTO;
import com.monk.commerce.task.dto.response.UpdatedCartDTO;
import com.monk.commerce.task.entity.*;
import com.monk.commerce.task.exception.CouponNotApplicableException;
import com.monk.commerce.task.util.Constants;
import com.monk.commerce.task.util.DiscountCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class CartWiseCouponStrategy implements CouponStrategy {

    @Override
    public boolean isApplicable(Coupon coupon, CartRequestDTO cart) {
        Objects.requireNonNull(coupon, "Coupon cannot be null");
        Objects.requireNonNull(cart, "Cart cannot be null");

        if (!(coupon instanceof CartWiseCoupon)) {
            return false;
        }

        CartWiseCoupon cartWiseCoupon = (CartWiseCoupon) coupon;
        BigDecimal cartTotal = calculateCartTotal(cart);

        return cartTotal.compareTo(cartWiseCoupon.getThresholdAmount()) >= 0;
    }

    @Override
    public BigDecimal calculateDiscount(Coupon coupon, CartRequestDTO cart) {
        Objects.requireNonNull(coupon, "Coupon cannot be null");
        Objects.requireNonNull(cart, "Cart cannot be null");

        if (!(coupon instanceof CartWiseCoupon)) {
            throw new IllegalArgumentException("Invalid coupon type for CartWiseCouponStrategy");
        }

        CartWiseCoupon cartWiseCoupon = (CartWiseCoupon) coupon;
        
        if (!isApplicable(coupon, cart)) {
            throw new CouponNotApplicableException(Constants.THRESHOLD_NOT_MET);
        }

        BigDecimal cartTotal = calculateCartTotal(cart);
        BigDecimal discount = DiscountCalculator.calculatePercentageDiscount(
            cartTotal, 
            cartWiseCoupon.getDiscountPercentage()
        );

        return DiscountCalculator.calculateDiscountWithCap(
            discount, 
            cartWiseCoupon.getMaxDiscountAmount()
        );
    }

    @Override
    public AppliedCouponResponseDTO applyCoupon(Coupon coupon, CartRequestDTO cart) {
        Objects.requireNonNull(coupon, "Coupon cannot be null");
        Objects.requireNonNull(cart, "Cart cannot be null");

        BigDecimal totalDiscount = calculateDiscount(coupon, cart);
        BigDecimal cartTotal = calculateCartTotal(cart);

        // Map cart items (no per-item discount for cart-wise)
        List<CartItemResponseDTO> responseItems = cart.getItems().stream()
                .map(item -> CartItemResponseDTO.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .totalDiscount(BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());

        UpdatedCartDTO updatedCart = UpdatedCartDTO.builder()
                .items(responseItems)
                .totalPrice(cartTotal)
                .totalDiscount(totalDiscount)
                .finalPrice(DiscountCalculator.calculateFinalPrice(cartTotal, totalDiscount))
                .build();

        return AppliedCouponResponseDTO.builder()
                .updatedCart(updatedCart)
                .build();
    }

    /**
     * Calculate total cart value
     */
    private BigDecimal calculateCartTotal(CartRequestDTO cart) {
        Objects.requireNonNull(cart, "Cart cannot be null");
        Objects.requireNonNull(cart.getItems(), "Cart items cannot be null");

        return cart.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
