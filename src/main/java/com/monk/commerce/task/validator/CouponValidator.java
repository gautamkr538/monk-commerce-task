package com.monk.commerce.task.validator;

import com.monk.commerce.task.entity.Coupon;
import com.monk.commerce.task.exception.InvalidCouponException;
import com.monk.commerce.task.util.Constants;
import com.monk.commerce.task.util.CouponUtil;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class CouponValidator {

    public void validateCouponValid(Coupon coupon) {
        Objects.requireNonNull(coupon, "Coupon cannot be null");

        if (!Boolean.TRUE.equals(coupon.getIsActive())) {
            throw new InvalidCouponException(Constants.COUPON_INACTIVE);
        }

        if (CouponUtil.isExpired(coupon)) {
            throw new InvalidCouponException(Constants.COUPON_EXPIRED);
        }

        if (CouponUtil.hasReachedMaxUsage(coupon)) {
            throw new InvalidCouponException("Coupon has reached maximum usage limit");
        }
    }

    public void validateCouponId(UUID couponId) {
        if (couponId == null) {
            throw new InvalidCouponException("Invalid coupon ID");
        }
    }
}
