package com.monk.commerce.task.exception;

public class CouponNotFoundException extends RuntimeException {
    
    public CouponNotFoundException(String message) {
        super(message);
    }
    
    public CouponNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public CouponNotFoundException(Long couponId) {
        super(String.format("Coupon not found with ID: %d", couponId));
    }
}
