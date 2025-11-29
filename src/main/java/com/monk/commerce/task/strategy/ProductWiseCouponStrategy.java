package com.monk.commerce.task.strategy;

import com.monk.commerce.task.dto.request.CartItemDTO;
import com.monk.commerce.task.dto.request.CartRequestDTO;
import com.monk.commerce.task.dto.response.AppliedCouponResponseDTO;
import com.monk.commerce.task.dto.response.CartItemResponseDTO;
import com.monk.commerce.task.dto.response.UpdatedCartDTO;
import com.monk.commerce.task.entity.Coupon;
import com.monk.commerce.task.entity.ProductWiseCoupon;
import com.monk.commerce.task.exception.CouponNotApplicableException;
import com.monk.commerce.task.util.CartUtil;
import com.monk.commerce.task.util.Constants;
import com.monk.commerce.task.util.CouponUtil;
import com.monk.commerce.task.util.DiscountCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProductWiseCouponStrategy implements CouponStrategy {

    private static final Logger log = LoggerFactory.getLogger(ProductWiseCouponStrategy.class);

    @Override
    public boolean isApplicable(Coupon coupon, CartRequestDTO cart) {
        Objects.requireNonNull(coupon, "Coupon cannot be null");
        Objects.requireNonNull(cart, "Cart cannot be null");
        if (!(coupon instanceof ProductWiseCoupon)) {
            return false;
        }
        ProductWiseCoupon productWiseCoupon = (ProductWiseCoupon) coupon;
        boolean hasTargetProduct = cart.getItems().stream()
                .anyMatch(item -> item.getProductId().equals(productWiseCoupon.getProductId()));
        if (!hasTargetProduct) {
            log.debug("Product {} not found in cart for coupon: {}", productWiseCoupon.getProductId(), coupon.getId());
            return false;
        }
        boolean notExcluded = !CouponUtil.isProductExcluded(coupon, productWiseCoupon.getProductId());
        log.debug("Product-wise coupon {} applicable: {}", coupon.getId(), notExcluded);
        return notExcluded;
    }

    @Override
    public BigDecimal calculateDiscount(Coupon coupon, CartRequestDTO cart) {
        Objects.requireNonNull(coupon, "Coupon cannot be null");
        Objects.requireNonNull(cart, "Cart cannot be null");
        if (!(coupon instanceof ProductWiseCoupon)) {
            throw new IllegalArgumentException("Invalid coupon type for ProductWiseCouponStrategy");
        }
        ProductWiseCoupon productWiseCoupon = (ProductWiseCoupon) coupon;
        if (!isApplicable(coupon, cart)) {
            log.error("Product {} not in cart for coupon: {}", productWiseCoupon.getProductId(), coupon.getId());
            throw new CouponNotApplicableException(Constants.PRODUCT_NOT_IN_CART);
        }
        Optional<CartItemDTO> targetItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productWiseCoupon.getProductId()))
                .findFirst();
        if (!targetItem.isPresent()) {
            return BigDecimal.ZERO;
        }
        CartItemDTO item = targetItem.get();
        BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        BigDecimal discount = DiscountCalculator.calculatePercentageDiscount(
                itemTotal,
                productWiseCoupon.getDiscountPercentage()
        );
        if (productWiseCoupon.getMaxDiscountPerProduct() != null) {
            BigDecimal maxTotalDiscount = productWiseCoupon.getMaxDiscountPerProduct()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            discount = discount.min(maxTotalDiscount);
        }
        log.info("Product-wise discount calculated: {} for product: {}", discount, item.getProductId());
        return discount;
    }

    @Override
    public AppliedCouponResponseDTO applyCoupon(Coupon coupon, CartRequestDTO cart) {
        Objects.requireNonNull(coupon, "Coupon cannot be null");
        Objects.requireNonNull(cart, "Cart cannot be null");
        log.debug("Applying product-wise coupon: {}", coupon.getId());
        ProductWiseCoupon productWiseCoupon = (ProductWiseCoupon) coupon;
        BigDecimal totalDiscount = calculateDiscount(coupon, cart);
        List<CartItemResponseDTO> responseItems = cart.getItems().stream()
                .map(item -> {
                    BigDecimal itemDiscount = BigDecimal.ZERO;
                    if (item.getProductId().equals(productWiseCoupon.getProductId())) {
                        BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                        itemDiscount = DiscountCalculator.calculatePercentageDiscount(
                                itemTotal,
                                productWiseCoupon.getDiscountPercentage()
                        );
                        if (productWiseCoupon.getMaxDiscountPerProduct() != null) {
                            BigDecimal maxTotalDiscount = productWiseCoupon.getMaxDiscountPerProduct()
                                    .multiply(BigDecimal.valueOf(item.getQuantity()));
                            itemDiscount = itemDiscount.min(maxTotalDiscount);
                        }
                    }
                    return CartItemResponseDTO.builder()
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .totalDiscount(itemDiscount)
                            .build();
                })
                .collect(Collectors.toList());
        BigDecimal cartTotal = CartUtil.calculateCartTotal(cart);
        UpdatedCartDTO updatedCart = UpdatedCartDTO.builder()
                .items(responseItems)
                .totalPrice(cartTotal)
                .totalDiscount(totalDiscount)
                .finalPrice(DiscountCalculator.calculateFinalPrice(cartTotal, totalDiscount))
                .build();
        log.info("Applied product-wise coupon with discount: {}", totalDiscount);
        return AppliedCouponResponseDTO.builder()
                .updatedCart(updatedCart)
                .build();
    }
}
