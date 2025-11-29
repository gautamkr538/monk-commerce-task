package com.monk.commerce.task.util;

import com.monk.commerce.task.dto.request.CartItemDTO;
import com.monk.commerce.task.dto.request.CartRequestDTO;
import com.monk.commerce.task.entity.BuyProduct;
import com.monk.commerce.task.entity.BxGyCoupon;
import com.monk.commerce.task.entity.Coupon;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

public final class CartUtil {

    private CartUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static BigDecimal calculateCartTotal(CartRequestDTO cart) {
        return cart.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal calculateEligibleCartTotal(Coupon coupon, CartRequestDTO cart) {
        return cart.getItems().stream()
                .filter(item -> !CouponUtil.isProductExcluded(coupon, item.getProductId()))
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static Map<Long, Integer> getCartProductQuantities(CartRequestDTO cart) {
        return cart.getItems().stream()
                .collect(Collectors.toMap(
                    CartItemDTO::getProductId,
                    CartItemDTO::getQuantity,
                    Integer::sum
                ));
    }

    public static Map<Long, BigDecimal> getProductPrices(CartRequestDTO cart) {
        return cart.getItems().stream()
                .collect(Collectors.toMap(
                    CartItemDTO::getProductId,
                    CartItemDTO::getPrice,
                    (price1, price2) -> price1
                ));
    }

    public static int calculateBuyQuantityInCart(BxGyCoupon coupon, Map<Long, Integer> cartQuantities, int tierLevel) {
        return coupon.getBuyProducts().stream()
                .filter(bp -> bp.getTierLevel() == tierLevel)
                .mapToInt(bp -> cartQuantities.getOrDefault(bp.getProductId(), 0))
                .sum();
    }

    public static int getTotalBuyQuantity(BxGyCoupon coupon, int tierLevel) {
        return coupon.getBuyProducts().stream()
                .filter(bp -> bp.getTierLevel() == tierLevel)
                .mapToInt(BuyProduct::getQuantity)
                .sum();
    }
}
