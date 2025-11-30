package com.monk.commerce.task.util;

import com.monk.commerce.task.dto.request.CartItemDTO;
import com.monk.commerce.task.dto.request.CartRequestDTO;

import com.monk.commerce.task.entity.BuyProduct;
import com.monk.commerce.task.entity.BxGyCoupon;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CartUtilTest {

    private CartRequestDTO buildCart() {
        CartRequestDTO cart = new CartRequestDTO();
        List<CartItemDTO> items = new ArrayList<>();

        CartItemDTO item1 = new CartItemDTO();
        item1.setProductId(1L);
        item1.setQuantity(2);
        item1.setPrice(BigDecimal.valueOf(100));

        CartItemDTO item2 = new CartItemDTO();
        item2.setProductId(2L);
        item2.setQuantity(1);
        item2.setPrice(BigDecimal.valueOf(50));

        items.add(item1);
        items.add(item2);
        cart.setItems(items);
        return cart;
    }

    @Test
    void calculateCartTotal_correctSumReturned() {
        CartRequestDTO cart = buildCart();

        BigDecimal total = CartUtil.calculateCartTotal(cart);

        assertEquals(BigDecimal.valueOf(250), total);
    }

    @Test
    void calculateEligibleCartTotal_excludesProductsCorrectly() {
        CartRequestDTO cart = buildCart();

        CouponStub coupon = new CouponStub(Set.of(2L));

        BigDecimal total = CartUtil.calculateEligibleCartTotal(coupon, cart);

        assertEquals(BigDecimal.valueOf(200), total);
    }

    static class CouponStub extends com.monk.commerce.task.entity.Coupon {
        Set<Long> excluded;

        CouponStub(Set<Long> excluded) {
            this.excluded = excluded;
        }

        @Override
        public List<com.monk.commerce.task.entity.ExcludedProduct> getExcludedProducts() {
            if (excluded == null) return Collections.emptyList();
            List<com.monk.commerce.task.entity.ExcludedProduct> list = new ArrayList<>();
            for(Long pid : excluded) {
                com.monk.commerce.task.entity.ExcludedProduct ex = new com.monk.commerce.task.entity.ExcludedProduct();
                ex.setProductId(pid);
                list.add(ex);
            }
            return list;
        }
    }

    @Test
    void getCartProductQuantities_correctQuantities() {
        CartRequestDTO cart = buildCart();

        Map<Long, Integer> map = CartUtil.getCartProductQuantities(cart);

        assertEquals(2, map.get(1L));
        assertEquals(1, map.get(2L));
    }

    @Test
    void getProductPrices_correctPrices() {
        CartRequestDTO cart = buildCart();

        Map<Long, BigDecimal> map = CartUtil.getProductPrices(cart);

        assertEquals(BigDecimal.valueOf(100), map.get(1L));
        assertEquals(BigDecimal.valueOf(50), map.get(2L));
    }

    @Test
    void calculateBuyQuantityInCart_correctlyAggregates() {
        BxGyCoupon coupon = new BxGyCoupon();
        List<BuyProduct> buys = new ArrayList<>();

        BuyProduct bp1 = BuyProduct.builder().productId(1L).quantity(1).tierLevel(1).build();
        BuyProduct bp2 = BuyProduct.builder().productId(2L).quantity(1).tierLevel(1).build();

        buys.add(bp1);
        buys.add(bp2);

        coupon.setBuyProducts(buys);

        Map<Long, Integer> cartQuantities = new HashMap<>();
        cartQuantities.put(1L, 2);
        cartQuantities.put(2L, 1);

        int qty = CartUtil.calculateBuyQuantityInCart(coupon, cartQuantities, 1);

        assertEquals(3, qty);
    }

    @Test
    void getTotalBuyQuantity_sumsCorrectly() {
        BxGyCoupon coupon = new BxGyCoupon();
        List<BuyProduct> buys = new ArrayList<>();

        buys.add(BuyProduct.builder().quantity(2).tierLevel(1).build());
        buys.add(BuyProduct.builder().quantity(3).tierLevel(1).build());

        coupon.setBuyProducts(buys);

        int result = CartUtil.getTotalBuyQuantity(coupon, 1);

        assertEquals(5, result);
    }
}
