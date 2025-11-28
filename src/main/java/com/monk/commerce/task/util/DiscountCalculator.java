package com.monk.commerce.task.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class DiscountCalculator {

    private DiscountCalculator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Calculate percentage discount
     */
    public static BigDecimal calculatePercentageDiscount(BigDecimal amount, BigDecimal percentage) {
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(percentage, "Percentage cannot be null");
        
        return amount.multiply(percentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate discount with maximum cap
     */
    public static BigDecimal calculateDiscountWithCap(BigDecimal calculatedDiscount, BigDecimal maxDiscount) {
        Objects.requireNonNull(calculatedDiscount, "Calculated discount cannot be null");
        
        if (maxDiscount == null) {
            return calculatedDiscount;
        }
        
        return calculatedDiscount.min(maxDiscount);
    }

    /**
     * Calculate final price after discount
     */
    public static BigDecimal calculateFinalPrice(BigDecimal originalPrice, BigDecimal discount) {
        Objects.requireNonNull(originalPrice, "Original price cannot be null");
        Objects.requireNonNull(discount, "Discount cannot be null");
        
        BigDecimal finalPrice = originalPrice.subtract(discount);
        return finalPrice.max(BigDecimal.ZERO);
    }
}
