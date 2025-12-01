package com.monk.commerce.task.util;

import com.monk.commerce.task.dto.request.CartRequestDTO;

import com.monk.commerce.task.entity.BuyProduct;
import com.monk.commerce.task.entity.BxGyCoupon;
import com.monk.commerce.task.entity.Coupon;
import com.monk.commerce.task.entity.CouponUsage;
import com.monk.commerce.task.entity.ExcludedProduct;
import com.monk.commerce.task.entity.GetProduct;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CouponUtilTest {

    @Test
    void isExpired_returnsTrueWhenDatePast() {
        Coupon coupon = new Coupon();
        coupon.setExpirationDate(LocalDateTime.now().minusDays(1));

        assertTrue(CouponUtil.isExpired(coupon));
    }

    @Test
    void isExpired_returnsFalseWhenNoExpiration() {
        Coupon coupon = new Coupon();
        coupon.setExpirationDate(null);

        assertFalse(CouponUtil.isExpired(coupon));
    }

    @Test
    void hasReachedMaxUsage_trueWhenUsageMeetsLimit() {
        Coupon coupon = new Coupon();
        coupon.setUsageCount(10L);
        coupon.setMaxUsageLimit(10L);

        assertTrue(CouponUtil.hasReachedMaxUsage(coupon));
    }

    @Test
    void hasUserReachedLimit_correctEvaluation() {
        Coupon coupon = new Coupon();
        coupon.setUsageLimitPerUser(3);

        CouponUsage u1 = CouponUsage.builder().userId("u1").usageCount(2).build();
        CouponUsage u2 = CouponUsage.builder().userId("u1").usageCount(1).build();
        coupon.setUsageHistory(Arrays.asList(u1, u2));

        assertTrue(CouponUtil.hasUserReachedLimit(coupon, "u1"));
    }

    @Test
    void isProductExcluded_trueWhenProductMatches() {
        Coupon coupon = new Coupon();
        ExcludedProduct ex = ExcludedProduct.builder().productId(1L).build();
        coupon.setExcludedProducts(List.of(ex));

        assertTrue(CouponUtil.isProductExcluded(coupon, 1L));
    }

    @Test
    void hasExcludedProducts_trueIfAnyExcludedInCart() {
        Coupon coupon = new Coupon();
        coupon.setExcludedProducts(List.of(ExcludedProduct.builder().productId(2L).build()));
        CartRequestDTO cart = new CartRequestDTO();
        cart.setItems(List.of(makeItem(1L), makeItem(2L)));
        assertTrue(CouponUtil.hasExcludedProducts(coupon, cart));
    }

    private com.monk.commerce.task.dto.request.CartItemDTO makeItem(Long id) {
        com.monk.commerce.task.dto.request.CartItemDTO dto = new com.monk.commerce.task.dto.request.CartItemDTO();
        dto.setProductId(id);
        dto.setQuantity(1);
        dto.setPrice(java.math.BigDecimal.TEN);
        return dto;
    }

    @Test
    void getUserUsageRemaining_correctCalculation() {
        Coupon coupon = new Coupon();
        coupon.setUsageLimitPerUser(5);

        CouponUsage usage = CouponUsage.builder()
                .userId("u1")
                .usageCount(3)
                .build();

        coupon.setUsageHistory(List.of(usage));

        assertEquals(2, CouponUtil.getUserUsageRemaining(coupon, "u1"));
    }

    @Test
    void getGlobalUsageRemaining_correctDifference() {
        Coupon coupon = new Coupon();
        coupon.setMaxUsageLimit(20L);
        coupon.setUsageCount(5L);

        assertEquals(15L, CouponUtil.getGlobalUsageRemaining(coupon));
    }

    @Test
    void getMaxTierLevel_correctMaxTier() {
        BxGyCoupon coupon = new BxGyCoupon();
        coupon.setBuyProducts(List.of(
                BuyProduct.builder().tierLevel(1).build(),
                BuyProduct.builder().tierLevel(3).build()
        ));
        coupon.setGetProducts(List.of(GetProduct.builder().tierLevel(2).build()));
        assertEquals(3, CouponUtil.getMaxTierLevel(coupon));
    }
}
