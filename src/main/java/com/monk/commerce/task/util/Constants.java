package com.monk.commerce.task.util;

public final class Constants {
    
    private Constants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // API Paths
    public static final String API_BASE_PATH = "/api/v1";
    public static final String COUPON_PATH = "/coupons";
    public static final String CART_PATH = "/cart";
    
    // Error Messages
    public static final String COUPON_NOT_FOUND = "Coupon not found with ID: %d";
    public static final String COUPON_EXPIRED = "Coupon has expired";
    public static final String COUPON_INACTIVE = "Coupon is not active";
    public static final String INVALID_CART = "Invalid cart data provided";
    public static final String EMPTY_CART = "Cart cannot be empty";
    public static final String COUPON_NOT_APPLICABLE = "Coupon is not applicable to this cart";
    public static final String THRESHOLD_NOT_MET = "Cart total does not meet the threshold amount";
    public static final String PRODUCT_NOT_IN_CART = "Required product not found in cart";
    public static final String BXGY_CONDITION_NOT_MET = "BxGy coupon conditions not met";
    
    // Validation Messages
    public static final String COUPON_TYPE_REQUIRED = "Coupon type is required";
    public static final String PRODUCT_ID_REQUIRED = "Product ID is required";
    public static final String QUANTITY_REQUIRED = "Quantity is required";
    public static final String QUANTITY_POSITIVE = "Quantity must be positive";
    public static final String PRICE_POSITIVE = "Price must be positive";

}
