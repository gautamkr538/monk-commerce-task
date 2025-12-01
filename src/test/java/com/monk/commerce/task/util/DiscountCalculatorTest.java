package com.monk.commerce.task.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DiscountCalculatorTest {

    @Test
    void calculatePercentageDiscount_correct() {
        BigDecimal result = DiscountCalculator.calculatePercentageDiscount(BigDecimal.valueOf(200), BigDecimal.valueOf(10));
        assertEquals(BigDecimal.valueOf(20.00).setScale(2), result);
    }

    @Test
    void calculateDiscountWithCap_appliesCap() {
        BigDecimal result = DiscountCalculator.calculateDiscountWithCap(BigDecimal.valueOf(100), BigDecimal.valueOf(50));
        assertEquals(BigDecimal.valueOf(50), result);
    }

    @Test
    void calculateDiscountWithCap_noCapReturnsOriginal() {
        BigDecimal result = DiscountCalculator.calculateDiscountWithCap(BigDecimal.valueOf(100), null);
        assertEquals(BigDecimal.valueOf(100), result);
    }

    @Test
    void calculateFinalPrice_neverBelowZero() {
        BigDecimal result = DiscountCalculator.calculateFinalPrice(BigDecimal.valueOf(50), BigDecimal.valueOf(100));
        assertEquals(BigDecimal.ZERO, result);
    }
}
