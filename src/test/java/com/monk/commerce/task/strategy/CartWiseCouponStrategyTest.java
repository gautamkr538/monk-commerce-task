package com.monk.commerce.task.strategy;

import com.monk.commerce.task.dto.request.CartItemDTO;
import com.monk.commerce.task.dto.request.CartRequestDTO;
import com.monk.commerce.task.dto.response.AppliedCouponResponseDTO;
import com.monk.commerce.task.dto.response.UpdatedCartDTO;
import com.monk.commerce.task.entity.CartWiseCoupon;
import com.monk.commerce.task.entity.Coupon;
import com.monk.commerce.task.entity.ExcludedProduct;
import com.monk.commerce.task.enums.CouponType;
import com.monk.commerce.task.exception.CouponNotApplicableException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CartWiseCouponStrategyTest {

    private final CartWiseCouponStrategy strategy = new CartWiseCouponStrategy();

    private CartRequestDTO buildCart(BigDecimal price1, BigDecimal price2) {
        CartRequestDTO cart = new CartRequestDTO();
        List<CartItemDTO> items = new ArrayList<>();

        CartItemDTO item1 = new CartItemDTO();
        item1.setProductId(1L);
        item1.setQuantity(1);
        item1.setPrice(price1);

        CartItemDTO item2 = new CartItemDTO();
        item2.setProductId(2L);
        item2.setQuantity(1);
        item2.setPrice(price2);

        items.add(item1);
        items.add(item2);
        cart.setItems(items);
        return cart;
    }

    private CartWiseCoupon buildCartWiseCoupon(BigDecimal threshold, BigDecimal discountPercent, BigDecimal maxDiscount) {
        CartWiseCoupon coupon = new CartWiseCoupon();
        coupon.setId(UUID.randomUUID());
        coupon.setCouponCode("CART10");
        coupon.setType(CouponType.CART_WISE);
        coupon.setIsActive(true);
        coupon.setThresholdAmount(threshold);
        coupon.setDiscountPercentage(discountPercent);
        coupon.setMaxDiscountAmount(maxDiscount);
        coupon.setExcludedProducts(new ArrayList<>());
        return coupon;
    }

    @Test
    void isApplicable_returnsFalse_whenNotCartWiseCoupon() {
        Coupon coupon = new Coupon();
        CartRequestDTO cart = new CartRequestDTO();
        cart.setItems(Collections.emptyList());

        assertFalse(strategy.isApplicable(coupon, cart));
    }

    @Test
    void isApplicable_returnsFalse_whenCartHasExcludedProduct() {
        CartWiseCoupon coupon = buildCartWiseCoupon(BigDecimal.valueOf(100), BigDecimal.valueOf(10), null);

        ExcludedProduct excluded = ExcludedProduct.builder()
                .id(UUID.randomUUID())
                .coupon(coupon)
                .productId(1L)
                .build();
        coupon.getExcludedProducts().add(excluded);

        CartRequestDTO cart = buildCart(BigDecimal.valueOf(80), BigDecimal.valueOf(30));

        boolean result = strategy.isApplicable(coupon, cart);

        assertFalse(result);
    }

    @Test
    void isApplicable_thresholdNotMet_returnsFalse() {
        CartWiseCoupon coupon = buildCartWiseCoupon(BigDecimal.valueOf(200), BigDecimal.valueOf(10), null);
        CartRequestDTO cart = buildCart(BigDecimal.valueOf(50), BigDecimal.valueOf(50)); // total 100

        boolean result = strategy.isApplicable(coupon, cart);

        assertFalse(result);
    }

    @Test
    void isApplicable_thresholdMet_returnsTrue() {
        CartWiseCoupon coupon = buildCartWiseCoupon(BigDecimal.valueOf(100), BigDecimal.valueOf(10), null);
        CartRequestDTO cart = buildCart(BigDecimal.valueOf(80), BigDecimal.valueOf(30)); // total 110

        boolean result = strategy.isApplicable(coupon, cart);

        assertTrue(result);
    }

    @Test
    void calculateDiscount_throwsForNonCartWiseCoupon() {
        Coupon coupon = new Coupon();
        CartRequestDTO cart = buildCart(BigDecimal.valueOf(100), BigDecimal.valueOf(100));

        assertThrows(IllegalArgumentException.class, () -> strategy.calculateDiscount(coupon, cart));
    }

    @Test
    void calculateDiscount_thresholdNotMet_throwsCouponNotApplicable() {
        CartWiseCoupon coupon = buildCartWiseCoupon(BigDecimal.valueOf(300), BigDecimal.valueOf(10), null);
        CartRequestDTO cart = buildCart(BigDecimal.valueOf(100), BigDecimal.valueOf(50)); // total 150

        assertThrows(CouponNotApplicableException.class, () -> strategy.calculateDiscount(coupon, cart));
    }

    @Test
    void calculateDiscount_respectsMaxDiscountCap() {
        CartWiseCoupon coupon = buildCartWiseCoupon(BigDecimal.valueOf(100), BigDecimal.valueOf(20), BigDecimal.valueOf(50));
        CartRequestDTO cart = buildCart(BigDecimal.valueOf(200), BigDecimal.valueOf(100)); // total 300

        // 20% of 300 = 60, but cap is 50
        BigDecimal discount = strategy.calculateDiscount(coupon, cart);

        assertEquals(BigDecimal.valueOf(50).setScale(2), discount);
    }

    @Test
    void applyCoupon_success_buildsUpdatedCart() {
        CartWiseCoupon coupon = buildCartWiseCoupon(BigDecimal.valueOf(100), BigDecimal.valueOf(10), null);
        CartRequestDTO cart = buildCart(BigDecimal.valueOf(100), BigDecimal.valueOf(100)); // total 200

        AppliedCouponResponseDTO response = strategy.applyCoupon(coupon, cart);

        assertNotNull(response);
        UpdatedCartDTO updatedCart = response.getUpdatedCart();
        assertNotNull(updatedCart);

        assertEquals(BigDecimal.valueOf(20.00).setScale(2), updatedCart.getTotalDiscount());
        assertEquals(BigDecimal.valueOf(180.00).setScale(2), updatedCart.getFinalPrice());
    }
}
