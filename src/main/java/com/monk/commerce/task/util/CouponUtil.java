package com.monk.commerce.task.util;

import com.monk.commerce.task.dto.request.CartRequestDTO;
import com.monk.commerce.task.entity.BxGyCoupon;
import com.monk.commerce.task.entity.Coupon;
import com.monk.commerce.task.entity.CouponUsage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class CouponUtil {

    private CouponUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isExpired(Coupon coupon) {
        if (coupon.getExpirationDate() == null) return false;
        return LocalDateTime.now().isAfter(coupon.getExpirationDate());
    }

    public static boolean isValid(Coupon coupon) {
        return Boolean.TRUE.equals(coupon.getIsActive()) && !isExpired(coupon);
    }

    public static boolean hasReachedMaxUsage(Coupon coupon) {
        if (coupon.getMaxUsageLimit() == null) return false;
        return coupon.getUsageCount() >= coupon.getMaxUsageLimit();
    }

    public static boolean hasUserReachedLimit(Coupon coupon, String userId) {
        if (coupon.getUsageLimitPerUser() == null || userId == null) return false;
        
        return coupon.getUsageHistory().stream()
                .filter(usage -> usage.getUserId().equals(userId))
                .mapToInt(CouponUsage::getUsageCount)
                .sum() >= coupon.getUsageLimitPerUser();
    }

    public static boolean isProductExcluded(Coupon coupon, Long productId) {
        if (productId == null || coupon.getExcludedProducts().isEmpty()) return false;
        
        return coupon.getExcludedProducts().stream()
                .anyMatch(ep -> ep.getProductId().equals(productId));
    }

    public static boolean hasExcludedProducts(Coupon coupon, CartRequestDTO cart) {
        if (coupon.getExcludedProducts().isEmpty()) return false;
        
        return cart.getItems().stream()
                .anyMatch(item -> isProductExcluded(coupon, item.getProductId()));
    }

    public static int getMaxTierLevel(BxGyCoupon coupon) {
        int maxBuyTier = coupon.getBuyProducts().stream()
                .mapToInt(bp -> bp.getTierLevel())
                .max()
                .orElse(1);
        
        int maxGetTier = coupon.getGetProducts().stream()
                .mapToInt(gp -> gp.getTierLevel())
                .max()
                .orElse(1);
        
        return Math.max(maxBuyTier, maxGetTier);
    }

    public static Integer getUserUsageRemaining(Coupon coupon, String userId) {
        if (coupon.getUsageLimitPerUser() == null) return null;
        
        int used = coupon.getUsageHistory().stream()
                .filter(usage -> usage.getUserId().equals(userId))
                .mapToInt(CouponUsage::getUsageCount)
                .sum();
        
        return Math.max(0, coupon.getUsageLimitPerUser() - used);
    }

    public static Long getGlobalUsageRemaining(Coupon coupon) {
        if (coupon.getMaxUsageLimit() == null) return null;
        return Math.max(0L, coupon.getMaxUsageLimit() - coupon.getUsageCount());
    }

    public static String generateCouponCode() {
        return "CPN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
