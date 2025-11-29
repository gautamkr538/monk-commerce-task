package com.monk.commerce.task.strategy;

import com.monk.commerce.task.dto.request.CartRequestDTO;
import com.monk.commerce.task.dto.response.AppliedCouponResponseDTO;
import com.monk.commerce.task.dto.response.CartItemResponseDTO;
import com.monk.commerce.task.dto.response.UpdatedCartDTO;
import com.monk.commerce.task.entity.CartWiseCoupon;
import com.monk.commerce.task.entity.Coupon;
import com.monk.commerce.task.exception.CouponNotApplicableException;
import com.monk.commerce.task.util.CartUtil;
import com.monk.commerce.task.util.Constants;
import com.monk.commerce.task.util.CouponUtil;
import com.monk.commerce.task.util.DiscountCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class CartWiseCouponStrategy implements CouponStrategy {

    private static final Logger log = LoggerFactory.getLogger(CartWiseCouponStrategy.class);

    @Override
    public boolean isApplicable(Coupon coupon, CartRequestDTO cart) {
        Objects.requireNonNull(coupon, "Coupon cannot be null");
        Objects.requireNonNull(cart, "Cart cannot be null");
        if (!(coupon instanceof CartWiseCoupon)) {
            return false;
        }
        if (CouponUtil.hasExcludedProducts(coupon, cart)) {
            log.debug("Cart contains excluded products for cart-wise coupon: {}", coupon.getId());
            return false;
        }
        CartWiseCoupon cartWiseCoupon = (CartWiseCoupon) coupon;
        BigDecimal eligibleCartTotal = CartUtil.calculateEligibleCartTotal(coupon, cart);
        boolean applicable = eligibleCartTotal.compareTo(cartWiseCoupon.getThresholdAmount()) >= 0;
        log.debug("Cart-wise coupon {} applicability: {} (Total: {}, Threshold: {})", coupon.getId(), applicable, eligibleCartTotal, cartWiseCoupon.getThresholdAmount());
        return applicable;
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
            log.error("Cart-wise coupon {} threshold not met", coupon.getId());
            throw new CouponNotApplicableException(Constants.THRESHOLD_NOT_MET);
        }
        BigDecimal eligibleCartTotal = CartUtil.calculateEligibleCartTotal(coupon, cart);
        BigDecimal discount = DiscountCalculator.calculatePercentageDiscount(
                eligibleCartTotal,
                cartWiseCoupon.getDiscountPercentage()
        );
        BigDecimal finalDiscount = DiscountCalculator.calculateDiscountWithCap(
                discount,
                cartWiseCoupon.getMaxDiscountAmount()
        );
        log.info("Cart-wise discount calculated: {} (before cap: {})", finalDiscount, discount);
        return finalDiscount;
    }

    @Override
    public AppliedCouponResponseDTO applyCoupon(Coupon coupon, CartRequestDTO cart) {
        Objects.requireNonNull(coupon, "Coupon cannot be null");
        Objects.requireNonNull(cart, "Cart cannot be null");
        log.debug("Applying cart-wise coupon: {}", coupon.getId());
        BigDecimal totalDiscount = calculateDiscount(coupon, cart);
        BigDecimal cartTotal = CartUtil.calculateCartTotal(cart);
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
        log.info("Applied cart-wise coupon with discount: {}", totalDiscount);
        return AppliedCouponResponseDTO.builder()
                .updatedCart(updatedCart)
                .build();
    }
}
