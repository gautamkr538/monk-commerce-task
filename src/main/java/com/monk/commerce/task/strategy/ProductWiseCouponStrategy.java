package com.monk.commerce.task.strategy;

import com.monk.commerce.task.dto.request.CartItemDTO;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProductWiseCouponStrategy implements CouponStrategy {

    @Override
    public boolean isApplicable(Coupon coupon, CartRequestDTO cart) {
        Objects.requireNonNull(coupon, "Coupon cannot be null");
        Objects.requireNonNull(cart, "Cart cannot be null");

        if (!(coupon instanceof ProductWiseCoupon)) {
            return false;
        }

        ProductWiseCoupon productWiseCoupon = (ProductWiseCoupon) coupon;
        
        return cart.getItems().stream()
                .anyMatch(item -> item.getProductId().equals(productWiseCoupon.getProductId()));
    }

    @Override
    public BigDecimal calculateDiscount(Coupon coupon, CartRequestDTO cart) {
        Objects.requireNonNull(coupon, "Coupon cannot be null");
        Objects.requireNonNull(cart, "Cart cannot be null");

        if (!(coupon instanceof ProductWiseCoupon)) {
            throw new IllegalArgumentException("Invalid coupon type for ProductWiseCouponStrategy");
        }

        ProductWiseCoupon productWiseCoupon = (ProductWiseCoupon) coupon;

        if (!isApplicable(coupon, cart)) {
            throw new CouponNotApplicableException(Constants.PRODUCT_NOT_IN_CART);
        }

        Optional<CartItemDTO> targetItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productWiseCoupon.getProductId()))
                .findFirst();

        if (!targetItem.isPresent()) {
            return BigDecimal.ZERO;
        }

        CartItemDTO item = targetItem.get();
        BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        
        return DiscountCalculator.calculatePercentageDiscount(
            itemTotal, 
            productWiseCoupon.getDiscountPercentage()
        );
    }

    @Override
    public AppliedCouponResponseDTO applyCoupon(Coupon coupon, CartRequestDTO cart) {
        Objects.requireNonNull(coupon, "Coupon cannot be null");
        Objects.requireNonNull(cart, "Cart cannot be null");

        ProductWiseCoupon productWiseCoupon = (ProductWiseCoupon) coupon;
        BigDecimal totalDiscount = calculateDiscount(coupon, cart);

        // Map cart items with discount applied to target product
        List<CartItemResponseDTO> responseItems = cart.getItems().stream()
                .map(item -> {
                    BigDecimal itemDiscount = BigDecimal.ZERO;
                    
                    if (item.getProductId().equals(productWiseCoupon.getProductId())) {
                        BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                        itemDiscount = DiscountCalculator.calculatePercentageDiscount(
                            itemTotal, 
                            productWiseCoupon.getDiscountPercentage()
                        );
                    }

                    return CartItemResponseDTO.builder()
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .totalDiscount(itemDiscount)
                            .build();
                })
                .collect(Collectors.toList());

        BigDecimal cartTotal = calculateCartTotal(cart);

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
