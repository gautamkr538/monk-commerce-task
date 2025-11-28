package com.monk.commerce.task.validator;

import com.monk.commerce.task.entity.Coupon;
import com.monk.commerce.task.exception.InvalidCouponException;
import com.monk.commerce.task.util.Constants;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
public class CouponValidator {

    /**
     * Validate coupon is not null
     */
    public void validateCouponNotNull(Coupon coupon) {
        if (Objects.isNull(coupon)) {
            throw new InvalidCouponException("Coupon cannot be null");
        }
    }

    /**
     * Validate coupon is active
     */
    public void validateCouponActive(Coupon coupon) {
        validateCouponNotNull(coupon);
        
        if (!Boolean.TRUE.equals(coupon.getIsActive())) {
            throw new InvalidCouponException(Constants.COUPON_INACTIVE);
        }
    }

    /**
     * Validate coupon is not expired
     */
    public void validateCouponNotExpired(Coupon coupon) {
        validateCouponNotNull(coupon);
        
        LocalDateTime expirationDate = coupon.getExpirationDate();
        if (Objects.nonNull(expirationDate) && LocalDateTime.now().isAfter(expirationDate)) {
            throw new InvalidCouponException(Constants.COUPON_EXPIRED);
        }
    }

    /**
     * Validate coupon is valid (active and not expired)
     */
    public void validateCouponValid(Coupon coupon) {
        validateCouponNotNull(coupon);
        validateCouponActive(coupon);
        validateCouponNotExpired(coupon);
    }

    /**
     * Validate coupon ID
     */
    public void validateCouponId(Long couponId) {
        if (Objects.isNull(couponId) || couponId <= 0) {
            throw new InvalidCouponException("Invalid coupon ID");
        }
    }
}
