package com.monk.commerce.task.strategy;

import com.monk.commerce.task.dto.request.CartItemDTO;
import com.monk.commerce.task.dto.request.CartRequestDTO;
import com.monk.commerce.task.dto.response.AppliedCouponResponseDTO;
import com.monk.commerce.task.dto.response.CartItemResponseDTO;
import com.monk.commerce.task.dto.response.UpdatedCartDTO;
import com.monk.commerce.task.entity.BuyProduct;
import com.monk.commerce.task.entity.BxGyCoupon;
import com.monk.commerce.task.entity.Coupon;
import com.monk.commerce.task.entity.GetProduct;
import com.monk.commerce.task.exception.CouponNotApplicableException;
import com.monk.commerce.task.util.Constants;
import com.monk.commerce.task.util.DiscountCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class BxGyCouponStrategy implements CouponStrategy {

    @Override
    public boolean isApplicable(Coupon coupon, CartRequestDTO cart) {
        Objects.requireNonNull(coupon, "Coupon cannot be null");
        Objects.requireNonNull(cart, "Cart cannot be null");

        if (!(coupon instanceof BxGyCoupon)) {
            return false;
        }

        BxGyCoupon bxGyCoupon = (BxGyCoupon) coupon;

        // Check if cart has required buy products
        Map<Long, Integer> cartProductQuantities = getCartProductQuantities(cart);
        int buyQuantityInCart = calculateBuyQuantityInCart(bxGyCoupon, cartProductQuantities);
        
        // Check if we have any get products in cart
        boolean hasGetProducts = bxGyCoupon.getGetProducts().stream()
                .anyMatch(gp -> cartProductQuantities.containsKey(gp.getProductId()));

        return buyQuantityInCart >= getTotalBuyQuantity(bxGyCoupon) && hasGetProducts;
    }

    @Override
    public BigDecimal calculateDiscount(Coupon coupon, CartRequestDTO cart) {
        Objects.requireNonNull(coupon, "Coupon cannot be null");
        Objects.requireNonNull(cart, "Cart cannot be null");

        if (!(coupon instanceof BxGyCoupon)) {
            throw new IllegalArgumentException("Invalid coupon type for BxGyCouponStrategy");
        }

        BxGyCoupon bxGyCoupon = (BxGyCoupon) coupon;

        if (!isApplicable(coupon, cart)) {
            throw new CouponNotApplicableException(Constants.BXGY_CONDITION_NOT_MET);
        }

        Map<Long, Integer> cartProductQuantities = getCartProductQuantities(cart);
        Map<Long, BigDecimal> productPrices = getProductPrices(cart);

        // Calculate how many times the offer can be applied
        int buyQuantityInCart = calculateBuyQuantityInCart(bxGyCoupon, cartProductQuantities);
        int totalBuyQuantity = getTotalBuyQuantity(bxGyCoupon);
        int possibleApplications = Math.min(
            buyQuantityInCart / totalBuyQuantity,
            bxGyCoupon.getRepetitionLimit()
        );

        // Calculate total discount from free products
        BigDecimal totalDiscount = BigDecimal.ZERO;
        
        for (GetProduct getProduct : bxGyCoupon.getGetProducts()) {
            Long productId = getProduct.getProductId();
            Integer quantityInCart = cartProductQuantities.getOrDefault(productId, 0);
            
            if (quantityInCart > 0) {
                int freeQuantity = Math.min(
                    getProduct.getQuantity() * possibleApplications,
                    quantityInCart
                );
                
                BigDecimal productPrice = productPrices.getOrDefault(productId, BigDecimal.ZERO);
                BigDecimal discount = productPrice.multiply(BigDecimal.valueOf(freeQuantity));
                totalDiscount = totalDiscount.add(discount);
            }
        }

        return totalDiscount;
    }

    @Override
    public AppliedCouponResponseDTO applyCoupon(Coupon coupon, CartRequestDTO cart) {
        Objects.requireNonNull(coupon, "Coupon cannot be null");
        Objects.requireNonNull(cart, "Cart cannot be null");

        BxGyCoupon bxGyCoupon = (BxGyCoupon) coupon;
        
        Map<Long, Integer> cartProductQuantities = getCartProductQuantities(cart);
        Map<Long, BigDecimal> productPrices = getProductPrices(cart);

        // Calculate applications
        int buyQuantityInCart = calculateBuyQuantityInCart(bxGyCoupon, cartProductQuantities);
        int totalBuyQuantity = getTotalBuyQuantity(bxGyCoupon);
        int possibleApplications = Math.min(
            buyQuantityInCart / totalBuyQuantity,
            bxGyCoupon.getRepetitionLimit()
        );

        // Calculate free quantities per product
        Map<Long, Integer> freeQuantities = new HashMap<>();
        for (GetProduct getProduct : bxGyCoupon.getGetProducts()) {
            Long productId = getProduct.getProductId();
            Integer quantityInCart = cartProductQuantities.getOrDefault(productId, 0);
            
            if (quantityInCart > 0) {
                int freeQuantity = Math.min(
                    getProduct.getQuantity() * possibleApplications,
                    quantityInCart
                );
                freeQuantities.put(productId, freeQuantity);
            }
        }

        // Build response items with discount
        BigDecimal totalDiscount = BigDecimal.ZERO;
        List<CartItemResponseDTO> responseItems = new ArrayList<>();

        for (CartItemDTO item : cart.getItems()) {
            Integer freeQty = freeQuantities.getOrDefault(item.getProductId(), 0);
            BigDecimal itemDiscount = item.getPrice().multiply(BigDecimal.valueOf(freeQty));
            totalDiscount = totalDiscount.add(itemDiscount);

            responseItems.add(CartItemResponseDTO.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .totalDiscount(itemDiscount)
                    .build());
        }

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
     * Get cart product quantities as map
     */
    private Map<Long, Integer> getCartProductQuantities(CartRequestDTO cart) {
        return cart.getItems().stream()
                .collect(Collectors.toMap(
                    CartItemDTO::getProductId,
                    CartItemDTO::getQuantity,
                    Integer::sum
                ));
    }

    /**
     * Get product prices from cart
     */
    private Map<Long, BigDecimal> getProductPrices(CartRequestDTO cart) {
        return cart.getItems().stream()
                .collect(Collectors.toMap(
                    CartItemDTO::getProductId,
                    CartItemDTO::getPrice,
                    (price1, price2) -> price1
                ));
    }

    /**
     * Calculate total buy quantity in cart
     */
    private int calculateBuyQuantityInCart(BxGyCoupon coupon, Map<Long, Integer> cartQuantities) {
        return coupon.getBuyProducts().stream()
                .mapToInt(bp -> cartQuantities.getOrDefault(bp.getProductId(), 0))
                .sum();
    }

    /**
     * Get total buy quantity required
     */
    private int getTotalBuyQuantity(BxGyCoupon coupon) {
        return coupon.getBuyProducts().stream()
                .mapToInt(BuyProduct::getQuantity)
                .sum();
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
