package com.monk.commerce.task.enums;

public enum CouponType {
    CART_WISE("cart-wise"),
    PRODUCT_WISE("product-wise"),
    BXGY("bxgy");

    private final String value;

    CouponType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CouponType fromValue(String value) {
        for (CouponType type : CouponType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid coupon type: " + value);
    }
}
