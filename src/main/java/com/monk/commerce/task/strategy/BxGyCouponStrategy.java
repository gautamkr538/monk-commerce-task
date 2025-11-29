package com.monk.commerce.task.strategy;

import com.monk.commerce.task.dto.request.CartItemDTO;
import com.monk.commerce.task.dto.request.CartRequestDTO;
import com.monk.commerce.task.dto.response.AppliedCouponResponseDTO;
import com.monk.commerce.task.dto.response.CartItemResponseDTO;
import com.monk.commerce.task.dto.response.UpdatedCartDTO;
import com.monk.commerce.task.entity.BxGyCoupon;
import com.monk.commerce.task.entity.Coupon;
import com.monk.commerce.task.entity.GetProduct;
import com.monk.commerce.task.exception.CouponNotApplicableException;
import com.monk.commerce.task.util.CartUtil;
import com.monk.commerce.task.util.Constants;
import com.monk.commerce.task.util.CouponUtil;
import com.monk.commerce.task.util.DiscountCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class BxGyCouponStrategy implements CouponStrategy {

    private static final Logger log = LoggerFactory.getLogger(BxGyCouponStrategy.class);

    @Override
    public boolean isApplicable(Coupon coupon, CartRequestDTO cart) {
        Objects.requireNonNull(coupon, "Coupon cannot be null");
        Objects.requireNonNull(cart, "Cart cannot be null");
        if (!(coupon instanceof BxGyCoupon)) {
            return false;
        }
        if (CouponUtil.hasExcludedProducts(coupon, cart)) {
            log.debug("Cart contains excluded products for BxGy coupon: {}", coupon.getId());
            return false;
        }
        BxGyCoupon bxGyCoupon = (BxGyCoupon) coupon;
        Map<Long, Integer> cartProductQuantities = CartUtil.getCartProductQuantities(cart);
        boolean isTiered = Boolean.TRUE.equals(bxGyCoupon.getIsTiered());
        log.debug("Checking BxGy applicability for coupon: {} (tiered: {})", coupon.getId(), isTiered);
        return isTiered ? isApplicableForTiered(bxGyCoupon, cartProductQuantities) : isApplicableForSimple(bxGyCoupon, cartProductQuantities);
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
            log.warn("BxGy coupon {} not applicable to cart", coupon.getId());
            throw new CouponNotApplicableException(Constants.BXGY_CONDITION_NOT_MET);
        }
        log.debug("Calculating discount for BxGy coupon: {}", coupon.getId());
        return Boolean.TRUE.equals(bxGyCoupon.getIsTiered()) ? calculateDiscountForTiered(bxGyCoupon, cart) : calculateDiscountForSimple(bxGyCoupon, cart);
    }

    @Override
    public AppliedCouponResponseDTO applyCoupon(Coupon coupon, CartRequestDTO cart) {
        Objects.requireNonNull(coupon, "Coupon cannot be null");
        Objects.requireNonNull(cart, "Cart cannot be null");
        BxGyCoupon bxGyCoupon = (BxGyCoupon) coupon;
        log.debug("Applying BxGy coupon: {} to cart", coupon.getId());
        return Boolean.TRUE.equals(bxGyCoupon.getIsTiered()) ? applyCouponForTiered(bxGyCoupon, cart) : applyCouponForSimple(bxGyCoupon, cart);
    }

    private boolean isApplicableForSimple(BxGyCoupon coupon, Map<Long, Integer> cartQuantities) {
        int buyQuantityInCart = CartUtil.calculateBuyQuantityInCart(coupon, cartQuantities, 1);
        int totalBuyQuantity = CartUtil.getTotalBuyQuantity(coupon, 1);
        log.debug("BxGy simple check - Buy quantity in cart: {}, Required: {}", buyQuantityInCart, totalBuyQuantity);
        if (buyQuantityInCart < totalBuyQuantity) {
            return false;
        }
        return coupon.getGetProducts().stream()
                .filter(gp -> gp.getTierLevel() == 1)
                .anyMatch(gp -> cartQuantities.containsKey(gp.getProductId()));
    }

    private boolean isApplicableForTiered(BxGyCoupon coupon, Map<Long, Integer> cartQuantities) {
        int maxTier = CouponUtil.getMaxTierLevel(coupon);
        log.debug("BxGy tiered check - Max tier: {}", maxTier);
        for (int tier = maxTier; tier >= 1; tier--) {
            int buyQuantityInCart = CartUtil.calculateBuyQuantityInCart(coupon, cartQuantities, tier);
            int totalBuyQuantity = CartUtil.getTotalBuyQuantity(coupon, tier);
            log.debug("Checking tier {} - Buy quantity: {}, Required: {}", tier, buyQuantityInCart, totalBuyQuantity);
            if (buyQuantityInCart >= totalBuyQuantity) {
                int finalTier = tier;
                boolean hasGetProducts = coupon.getGetProducts().stream()
                        .filter(gp -> gp.getTierLevel() == finalTier)
                        .anyMatch(gp -> cartQuantities.containsKey(gp.getProductId()));
                if (hasGetProducts) {
                    log.debug("Tier {} is applicable", tier);
                    return true;
                }
            }
        }
        return false;
    }

    private BigDecimal calculateDiscountForSimple(BxGyCoupon coupon, CartRequestDTO cart) {
        Map<Long, Integer> cartProductQuantities = CartUtil.getCartProductQuantities(cart);
        Map<Long, BigDecimal> productPrices = CartUtil.getProductPrices(cart);
        int buyQuantityInCart = CartUtil.calculateBuyQuantityInCart(coupon, cartProductQuantities, 1);
        int totalBuyQuantity = CartUtil.getTotalBuyQuantity(coupon, 1);
        int possibleApplications = Math.min(buyQuantityInCart / totalBuyQuantity, coupon.getRepetitionLimit());
        log.debug("BxGy simple - Possible applications: {}", possibleApplications);
        BigDecimal totalDiscount = BigDecimal.ZERO;
        for (GetProduct getProduct : coupon.getGetProducts()) {
            if (getProduct.getTierLevel() != 1) continue;
            Long productId = getProduct.getProductId();
            Integer quantityInCart = cartProductQuantities.getOrDefault(productId, 0);
            if (quantityInCart > 0) {
                int freeQuantity = Math.min(getProduct.getQuantity() * possibleApplications, quantityInCart);
                BigDecimal productPrice = productPrices.getOrDefault(productId, BigDecimal.ZERO);
                BigDecimal discount = productPrice.multiply(BigDecimal.valueOf(freeQuantity));
                totalDiscount = totalDiscount.add(discount);
                log.debug("Free quantity for product {}: {}, Discount: {}", productId, freeQuantity, discount);
            }
        }
        log.info("BxGy simple discount calculated: {}", totalDiscount);
        return totalDiscount;
    }

    private BigDecimal calculateDiscountForTiered(BxGyCoupon coupon, CartRequestDTO cart) {
        Map<Long, Integer> cartProductQuantities = CartUtil.getCartProductQuantities(cart);
        Map<Long, BigDecimal> productPrices = CartUtil.getProductPrices(cart);
        int maxTier = CouponUtil.getMaxTierLevel(coupon);
        for (int tier = maxTier; tier >= 1; tier--) {
            int buyQuantityInCart = CartUtil.calculateBuyQuantityInCart(coupon, cartProductQuantities, tier);
            int totalBuyQuantity = CartUtil.getTotalBuyQuantity(coupon, tier);
            if (buyQuantityInCart >= totalBuyQuantity) {
                int possibleApplications = Math.min(buyQuantityInCart / totalBuyQuantity, coupon.getRepetitionLimit());
                log.debug("BxGy tiered - Tier {}, Possible applications: {}", tier, possibleApplications);
                BigDecimal tierDiscount = BigDecimal.ZERO;
                for (GetProduct getProduct : coupon.getGetProducts()) {
                    if (getProduct.getTierLevel() != tier) continue;
                    Long productId = getProduct.getProductId();
                    Integer quantityInCart = cartProductQuantities.getOrDefault(productId, 0);
                    if (quantityInCart > 0) {
                        int freeQuantity = Math.min(getProduct.getQuantity() * possibleApplications, quantityInCart);
                        BigDecimal productPrice = productPrices.getOrDefault(productId, BigDecimal.ZERO);
                        BigDecimal discount = productPrice.multiply(BigDecimal.valueOf(freeQuantity));
                        tierDiscount = tierDiscount.add(discount);
                    }
                }
                if (tierDiscount.compareTo(BigDecimal.ZERO) > 0) {
                    log.info("Applied tier {} for BxGy coupon: {} with discount: {}", tier, coupon.getId(), tierDiscount);
                    return tierDiscount;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private AppliedCouponResponseDTO applyCouponForSimple(BxGyCoupon coupon, CartRequestDTO cart) {
        Map<Long, Integer> cartProductQuantities = CartUtil.getCartProductQuantities(cart);
        int buyQuantityInCart = CartUtil.calculateBuyQuantityInCart(coupon, cartProductQuantities, 1);
        int totalBuyQuantity = CartUtil.getTotalBuyQuantity(coupon, 1);
        int possibleApplications = Math.min(buyQuantityInCart / totalBuyQuantity, coupon.getRepetitionLimit());
        Map<Long, Integer> freeQuantities = new HashMap<>();
        for (GetProduct getProduct : coupon.getGetProducts()) {
            if (getProduct.getTierLevel() != 1) continue;
            Long productId = getProduct.getProductId();
            Integer quantityInCart = cartProductQuantities.getOrDefault(productId, 0);
            if (quantityInCart > 0) {
                int freeQuantity = Math.min(getProduct.getQuantity() * possibleApplications, quantityInCart);
                freeQuantities.put(productId, freeQuantity);
            }
        }
        log.info("Applied BxGy simple coupon with {} free items", freeQuantities.size());
        return buildResponse(cart, freeQuantities);
    }

    private AppliedCouponResponseDTO applyCouponForTiered(BxGyCoupon coupon, CartRequestDTO cart) {
        Map<Long, Integer> cartProductQuantities = CartUtil.getCartProductQuantities(cart);
        int maxTier = CouponUtil.getMaxTierLevel(coupon);
        for (int tier = maxTier; tier >= 1; tier--) {
            int buyQuantityInCart = CartUtil.calculateBuyQuantityInCart(coupon, cartProductQuantities, tier);
            int totalBuyQuantity = CartUtil.getTotalBuyQuantity(coupon, tier);
            if (buyQuantityInCart >= totalBuyQuantity) {
                int possibleApplications = Math.min(buyQuantityInCart / totalBuyQuantity, coupon.getRepetitionLimit());
                Map<Long, Integer> freeQuantities = new HashMap<>();
                for (GetProduct getProduct : coupon.getGetProducts()) {
                    if (getProduct.getTierLevel() != tier) continue;
                    Long productId = getProduct.getProductId();
                    Integer quantityInCart = cartProductQuantities.getOrDefault(productId, 0);
                    if (quantityInCart > 0) {
                        int freeQuantity = Math.min(getProduct.getQuantity() * possibleApplications, quantityInCart);
                        freeQuantities.put(productId, freeQuantity);
                    }
                }
                if (!freeQuantities.isEmpty()) {
                    log.info("Applied BxGy tiered coupon at tier {} with {} free items", tier, freeQuantities.size());
                    return buildResponse(cart, freeQuantities);
                }
            }
        }
        return buildResponse(cart, new HashMap<>());
    }

    private AppliedCouponResponseDTO buildResponse(CartRequestDTO cart, Map<Long, Integer> freeQuantities) {
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
        BigDecimal cartTotal = CartUtil.calculateCartTotal(cart);
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
}
