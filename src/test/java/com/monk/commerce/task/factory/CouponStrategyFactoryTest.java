package com.monk.commerce.task.factory;

import com.monk.commerce.task.enums.CouponType;
import com.monk.commerce.task.exception.InvalidCouponException;
import com.monk.commerce.task.strategy.BxGyCouponStrategy;
import com.monk.commerce.task.strategy.CartWiseCouponStrategy;
import com.monk.commerce.task.strategy.ProductWiseCouponStrategy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CouponStrategyFactoryTest {

    @Test
    void getStrategy_returnsCorrectStrategies() {
        CartWiseCouponStrategy cart = new CartWiseCouponStrategy();
        ProductWiseCouponStrategy prod = new ProductWiseCouponStrategy();
        BxGyCouponStrategy bxgy = new BxGyCouponStrategy();

        CouponStrategyFactory factory = new CouponStrategyFactory(cart, prod, bxgy);

        assertEquals(cart, factory.getStrategy(CouponType.CART_WISE));
        assertEquals(prod, factory.getStrategy(CouponType.PRODUCT_WISE));
        assertEquals(bxgy, factory.getStrategy(CouponType.BXGY));
    }

    @Test
    void getStrategy_throwsForInvalidType() {
        CartWiseCouponStrategy cart = new CartWiseCouponStrategy();
        ProductWiseCouponStrategy prod = new ProductWiseCouponStrategy();
        BxGyCouponStrategy bxgy = new BxGyCouponStrategy();

        CouponStrategyFactory factory = new CouponStrategyFactory(cart, prod, bxgy);

        assertThrows(InvalidCouponException.class, () -> factory.getStrategy(null));
    }
}
