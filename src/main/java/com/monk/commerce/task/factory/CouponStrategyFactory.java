package com.monk.commerce.task.factory;

import com.monk.commerce.task.enums.CouponType;
import com.monk.commerce.task.exception.InvalidCouponException;
import com.monk.commerce.task.strategy.BxGyCouponStrategy;
import com.monk.commerce.task.strategy.CartWiseCouponStrategy;
import com.monk.commerce.task.strategy.CouponStrategy;
import com.monk.commerce.task.strategy.ProductWiseCouponStrategy;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CouponStrategyFactory {

    private final CartWiseCouponStrategy cartWiseCouponStrategy;
    private final ProductWiseCouponStrategy productWiseCouponStrategy;
    private final BxGyCouponStrategy bxGyCouponStrategy;

    public CouponStrategyFactory(
            CartWiseCouponStrategy cartWiseCouponStrategy,
            ProductWiseCouponStrategy productWiseCouponStrategy,
            BxGyCouponStrategy bxGyCouponStrategy) {
        this.cartWiseCouponStrategy = cartWiseCouponStrategy;
        this.productWiseCouponStrategy = productWiseCouponStrategy;
        this.bxGyCouponStrategy = bxGyCouponStrategy;
    }

    public CouponStrategy getStrategy(CouponType couponType) {
        Objects.requireNonNull(couponType, "Coupon type cannot be null");

        return switch (couponType) {
            case CART_WISE -> cartWiseCouponStrategy;
            case PRODUCT_WISE -> productWiseCouponStrategy;
            case BXGY -> bxGyCouponStrategy;
            default -> throw new InvalidCouponException("Unsupported coupon type: " + couponType);
        };
    }
}
