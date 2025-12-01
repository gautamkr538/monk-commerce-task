package com.monk.commerce.task.validator;

import com.monk.commerce.task.entity.Coupon;
import com.monk.commerce.task.exception.InvalidCouponException;
import com.monk.commerce.task.util.Constants;
import com.monk.commerce.task.util.CouponUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

class CouponValidatorTest {

    private CouponValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CouponValidator();
    }

    private Coupon buildCoupon(Boolean active) {
        Coupon coupon = new Coupon();
        coupon.setIsActive(active);
        return coupon;
    }

    @Test
    void validateCouponValid_nullCoupon_throwsException() {
        NullPointerException ex = assertThrows(NullPointerException.class, () -> validator.validateCouponValid(null));
        assertEquals("Coupon cannot be null", ex.getMessage());
    }

    @Test
    void validateCouponValid_couponInactive_throwsException() {
        Coupon coupon = buildCoupon(false);
        InvalidCouponException ex = assertThrows(InvalidCouponException.class, () -> validator.validateCouponValid(coupon));
        assertEquals(Constants.COUPON_INACTIVE, ex.getMessage());
    }

    @Test
    void validateCouponValid_couponExpired_throwsException() {
        Coupon coupon = buildCoupon(true);
        try (MockedStatic<CouponUtil> util = mockStatic(CouponUtil.class)) {
            util.when(() -> CouponUtil.isExpired(coupon)).thenReturn(true);
            util.when(() -> CouponUtil.hasReachedMaxUsage(coupon)).thenReturn(false);
            InvalidCouponException ex = assertThrows(InvalidCouponException.class, () -> validator.validateCouponValid(coupon));
            assertEquals(Constants.COUPON_EXPIRED, ex.getMessage());
        }
    }

    @Test
    void validateCouponValid_couponReachedMaxUsage_throwsException() {
        Coupon coupon = buildCoupon(true);

        try (MockedStatic<CouponUtil> util = mockStatic(CouponUtil.class)) {
            util.when(() -> CouponUtil.isExpired(coupon)).thenReturn(false);
            util.when(() -> CouponUtil.hasReachedMaxUsage(coupon)).thenReturn(true);
            InvalidCouponException ex = assertThrows(InvalidCouponException.class, () -> validator.validateCouponValid(coupon));
            assertEquals("Coupon has reached maximum usage limit", ex.getMessage());
        }
    }

    @Test
    void validateCouponValid_validCoupon_doesNotThrow() {
        Coupon coupon = buildCoupon(true);

        try (MockedStatic<CouponUtil> util = mockStatic(CouponUtil.class)) {
            util.when(() -> CouponUtil.isExpired(coupon)).thenReturn(false);
            util.when(() -> CouponUtil.hasReachedMaxUsage(coupon)).thenReturn(false);

            assertDoesNotThrow(() -> validator.validateCouponValid(coupon));
        }
    }

    @Test
    void validateCouponId_nullId_throwsException() {
        InvalidCouponException ex = assertThrows(InvalidCouponException.class, () -> validator.validateCouponId(null));

        assertEquals("Invalid coupon ID", ex.getMessage());
    }

    @Test
    void validateCouponId_validId_doesNotThrow() {
        UUID id = UUID.randomUUID();
        assertDoesNotThrow(() -> validator.validateCouponId(id));
    }
}
