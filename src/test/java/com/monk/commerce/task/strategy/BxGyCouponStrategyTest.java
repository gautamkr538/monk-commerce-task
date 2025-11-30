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
import com.monk.commerce.task.enums.CouponType;
import com.monk.commerce.task.exception.CouponNotApplicableException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BxGyCouponStrategyTest {

    private final BxGyCouponStrategy strategy = new BxGyCouponStrategy();

    private CartRequestDTO buildCartSimple() {
        CartRequestDTO cart = new CartRequestDTO();
        List<CartItemDTO> items = new ArrayList<>();

        CartItemDTO buyItem = new CartItemDTO();
        buyItem.setProductId(1L);
        buyItem.setQuantity(2);
        buyItem.setPrice(BigDecimal.valueOf(100));

        CartItemDTO getItem = new CartItemDTO();
        getItem.setProductId(2L);
        getItem.setQuantity(1);
        getItem.setPrice(BigDecimal.valueOf(50));

        items.add(buyItem);
        items.add(getItem);
        cart.setItems(items);
        cart.setUserId("user1");
        return cart;
    }

    private BxGyCoupon buildSimpleBxGyCoupon() {
        BxGyCoupon coupon = BxGyCoupon.builder()
                .id(UUID.randomUUID())
                .couponCode("BXGY1")
                .type(CouponType.BXGY)
                .isActive(true)
                .repetitionLimit(1)
                .isTiered(false)
                .buyProducts(new ArrayList<>())
                .getProducts(new ArrayList<>())
                .build();

        BuyProduct buyProduct = BuyProduct.builder()
                .id(UUID.randomUUID())
                .bxgyCoupon(coupon)
                .productId(1L)
                .quantity(2)
                .tierLevel(1)
                .build();

        GetProduct getProduct = GetProduct.builder()
                .id(UUID.randomUUID())
                .bxgyCoupon(coupon)
                .productId(2L)
                .quantity(1)
                .tierLevel(1)
                .build();

        coupon.getBuyProducts().add(buyProduct);
        coupon.getGetProducts().add(getProduct);
        return coupon;
    }

    @Test
    void isApplicable_returnsFalse_whenNotBxGyCoupon() {
        Coupon coupon = new Coupon();
        CartRequestDTO cart = new CartRequestDTO();
        cart.setItems(Collections.emptyList());

        assertFalse(strategy.isApplicable(coupon, cart));
    }

    @Test
    void isApplicable_simpleScenario_trueWhenConditionsMet() {
        BxGyCoupon coupon = buildSimpleBxGyCoupon();
        CartRequestDTO cart = buildCartSimple();

        boolean result = strategy.isApplicable(coupon, cart);

        assertTrue(result);
    }

    @Test
    void isApplicable_simpleScenario_falseWhenBuyQuantityInsufficient() {
        BxGyCoupon coupon = buildSimpleBxGyCoupon();

        CartRequestDTO cart = new CartRequestDTO();
        List<CartItemDTO> items = new ArrayList<>();
        CartItemDTO buyItem = new CartItemDTO();
        buyItem.setProductId(1L);
        buyItem.setQuantity(1);
        buyItem.setPrice(BigDecimal.valueOf(100));
        items.add(buyItem);
        cart.setItems(items);

        boolean result = strategy.isApplicable(coupon, cart);

        assertFalse(result);
    }

    @Test
    void calculateDiscount_throwsForNonBxGyCoupon() {
        Coupon coupon = new Coupon();
        CartRequestDTO cart = buildCartSimple();

        assertThrows(IllegalArgumentException.class, () -> strategy.calculateDiscount(coupon, cart));
    }

    @Test
    void calculateDiscount_simpleScenario_returnsExpectedDiscount() {
        BxGyCoupon coupon = buildSimpleBxGyCoupon();
        CartRequestDTO cart = buildCartSimple();

        // buy 2 of product 1, get 1 of product 2 free (= 50 discount)
        BigDecimal discount = strategy.calculateDiscount(coupon, cart);

        assertEquals(BigDecimal.valueOf(50), discount);
    }

    @Test
    void calculateDiscount_notApplicable_throwsCouponNotApplicableException() {
        BxGyCoupon coupon = buildSimpleBxGyCoupon();

        CartRequestDTO cart = new CartRequestDTO();
        cart.setItems(Collections.emptyList());

        assertThrows(CouponNotApplicableException.class, () -> strategy.calculateDiscount(coupon, cart));
    }

    @Test
    void applyCoupon_simpleScenario_appliesFreeItemDiscount() {
        BxGyCoupon coupon = buildSimpleBxGyCoupon();
        CartRequestDTO cart = buildCartSimple();

        AppliedCouponResponseDTO response = strategy.applyCoupon(coupon, cart);

        assertNotNull(response);
        UpdatedCartDTO updatedCart = response.getUpdatedCart();
        assertNotNull(updatedCart);
        assertEquals(BigDecimal.valueOf(50), updatedCart.getTotalDiscount());

        Optional<CartItemResponseDTO> getItemResponse = updatedCart.getItems().stream()
                .filter(i -> i.getProductId().equals(2L))
                .findFirst();

        assertTrue(getItemResponse.isPresent());
        assertEquals(BigDecimal.valueOf(50), getItemResponse.get().getTotalDiscount());
    }
}
