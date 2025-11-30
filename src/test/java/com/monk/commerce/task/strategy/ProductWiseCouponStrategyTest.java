package com.monk.commerce.task.strategy;

import com.monk.commerce.task.dto.request.CartItemDTO;
import com.monk.commerce.task.dto.request.CartRequestDTO;
import com.monk.commerce.task.dto.response.AppliedCouponResponseDTO;
import com.monk.commerce.task.dto.response.CartItemResponseDTO;
import com.monk.commerce.task.dto.response.UpdatedCartDTO;
import com.monk.commerce.task.entity.Coupon;
import com.monk.commerce.task.entity.ExcludedProduct;
import com.monk.commerce.task.entity.ProductWiseCoupon;
import com.monk.commerce.task.enums.CouponType;
import com.monk.commerce.task.exception.CouponNotApplicableException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductWiseCouponStrategyTest {

    private final ProductWiseCouponStrategy strategy = new ProductWiseCouponStrategy();

    private CartRequestDTO buildCartWithProduct(Long productId, int quantity, BigDecimal price) {
        CartRequestDTO cart = new CartRequestDTO();
        List<CartItemDTO> items = new ArrayList<>();

        CartItemDTO item = new CartItemDTO();
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setPrice(price);

        items.add(item);
        cart.setItems(items);
        return cart;
    }

    private ProductWiseCoupon buildProductWiseCoupon(Long productId, BigDecimal discountPercent, BigDecimal maxPerProduct) {
        ProductWiseCoupon coupon = new ProductWiseCoupon();
        coupon.setId(UUID.randomUUID());
        coupon.setCouponCode("PROD10");
        coupon.setType(CouponType.PRODUCT_WISE);
        coupon.setIsActive(true);
        coupon.setProductId(productId);
        coupon.setDiscountPercentage(discountPercent);
        coupon.setMaxDiscountPerProduct(maxPerProduct);
        coupon.setExcludedProducts(new ArrayList<>());
        return coupon;
    }

    @Test
    void isApplicable_returnsFalse_whenNotProductWiseCoupon() {
        Coupon coupon = new Coupon();
        CartRequestDTO cart = buildCartWithProduct(1L, 1, BigDecimal.valueOf(100));

        assertFalse(strategy.isApplicable(coupon, cart));
    }

    @Test
    void isApplicable_returnsFalse_whenProductNotInCart() {
        ProductWiseCoupon coupon = buildProductWiseCoupon(2L, BigDecimal.valueOf(10), null);
        CartRequestDTO cart = buildCartWithProduct(1L, 1, BigDecimal.valueOf(100));

        boolean result = strategy.isApplicable(coupon, cart);

        assertFalse(result);
    }

    @Test
    void isApplicable_returnsFalse_whenProductExcluded() {
        ProductWiseCoupon coupon = buildProductWiseCoupon(1L, BigDecimal.valueOf(10), null);
        ExcludedProduct excludedProduct = ExcludedProduct.builder()
                .id(UUID.randomUUID())
                .coupon(coupon)
                .productId(1L)
                .build();
        coupon.getExcludedProducts().add(excludedProduct);

        CartRequestDTO cart = buildCartWithProduct(1L, 1, BigDecimal.valueOf(100));

        boolean result = strategy.isApplicable(coupon, cart);

        assertFalse(result);
    }

    @Test
    void isApplicable_returnsTrue_whenProductPresentAndNotExcluded() {
        ProductWiseCoupon coupon = buildProductWiseCoupon(1L, BigDecimal.valueOf(10), null);
        CartRequestDTO cart = buildCartWithProduct(1L, 2, BigDecimal.valueOf(100));

        boolean result = strategy.isApplicable(coupon, cart);

        assertTrue(result);
    }

    @Test
    void calculateDiscount_throwsForNonProductWiseCoupon() {
        Coupon coupon = new Coupon();
        CartRequestDTO cart = buildCartWithProduct(1L, 1, BigDecimal.valueOf(100));

        assertThrows(IllegalArgumentException.class, () -> strategy.calculateDiscount(coupon, cart));
    }

    @Test
    void calculateDiscount_notApplicable_throwsCouponNotApplicableException() {
        ProductWiseCoupon coupon = buildProductWiseCoupon(2L, BigDecimal.valueOf(10), null);
        CartRequestDTO cart = buildCartWithProduct(1L, 1, BigDecimal.valueOf(100));

        assertThrows(CouponNotApplicableException.class, () -> strategy.calculateDiscount(coupon, cart));
    }

    @Test
    void calculateDiscount_respectsMaxDiscountPerProduct() {
        ProductWiseCoupon coupon = buildProductWiseCoupon(1L, BigDecimal.valueOf(50), BigDecimal.valueOf(30));
        CartRequestDTO cart = buildCartWithProduct(1L, 2, BigDecimal.valueOf(100));

        // item total = 200; 50% = 100; max per product = 30 * 2 = 60; so discount = 60
        BigDecimal discount = strategy.calculateDiscount(coupon, cart);

        assertEquals(BigDecimal.valueOf(60).setScale(2), discount);
    }

    @Test
    void applyCoupon_success_appliesDiscountToTargetProduct() {
        ProductWiseCoupon coupon = buildProductWiseCoupon(1L, BigDecimal.valueOf(10), null);
        CartRequestDTO cart = buildCartWithProduct(1L, 2, BigDecimal.valueOf(100)); // total 200, 10% = 20

        AppliedCouponResponseDTO response = strategy.applyCoupon(coupon, cart);

        assertNotNull(response);
        UpdatedCartDTO updatedCart = response.getUpdatedCart();
        assertNotNull(updatedCart);

        assertEquals(BigDecimal.valueOf(20).setScale(2), updatedCart.getTotalDiscount());

        CartItemResponseDTO item = updatedCart.getItems().get(0);
        assertEquals(1L, item.getProductId());
        assertEquals(BigDecimal.valueOf(20).setScale(2), item.getTotalDiscount());
    }
}
