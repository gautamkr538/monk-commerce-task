package com.monk.commerce.task.exception;

public class CouponNotApplicableException extends RuntimeException {
    
    public CouponNotApplicableException(String message) {
        super(message);
    }
    
    public CouponNotApplicableException(String message, Throwable cause) {
        super(message, cause);
    }
}
