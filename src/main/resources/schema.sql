-- Drop existing tables (in correct order due to foreign keys)
DROP TABLE IF EXISTS get_product CASCADE;
DROP TABLE IF EXISTS buy_product CASCADE;
DROP TABLE IF EXISTS bxgy_coupon CASCADE;
DROP TABLE IF EXISTS product_wise_coupon CASCADE;
DROP TABLE IF EXISTS cart_wise_coupon CASCADE;
DROP TABLE IF EXISTS coupon CASCADE;

-- Base Coupon Table (matches Coupon.java)
CREATE TABLE coupon (
                        id BIGSERIAL PRIMARY KEY,
                        coupon_code VARCHAR(50) UNIQUE NOT NULL,
                        type VARCHAR(20) NOT NULL,
                        description VARCHAR(500),
                        is_active BOOLEAN NOT NULL DEFAULT TRUE,
                        expiration_date TIMESTAMP,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Cart-Wise Coupon Table (matches CartWiseCoupon.java)
CREATE TABLE cart_wise_coupon (
                                  id BIGINT PRIMARY KEY REFERENCES coupon(id) ON DELETE CASCADE,
                                  threshold_amount DECIMAL(10,2) NOT NULL,
                                  discount_percentage DECIMAL(5,2),
                                  max_discount_amount DECIMAL(10,2)
);

-- Product-Wise Coupon Table (matches ProductWiseCoupon.java)
CREATE TABLE product_wise_coupon (
                                     id BIGINT PRIMARY KEY REFERENCES coupon(id) ON DELETE CASCADE,
                                     product_id BIGINT NOT NULL,
                                     discount_percentage DECIMAL(5,2),
                                     max_discount_per_product DECIMAL(10,2)
);

-- BxGy Coupon Table (matches BxGyCoupon.java)
CREATE TABLE bxgy_coupon (
                             id BIGINT PRIMARY KEY REFERENCES coupon(id) ON DELETE CASCADE,
                             repetition_limit INTEGER NOT NULL
);

-- Buy Products Table (matches BuyProduct.java)
CREATE TABLE buy_product (
                             id BIGSERIAL PRIMARY KEY,
                             bxgy_coupon_id BIGINT NOT NULL REFERENCES bxgy_coupon(id) ON DELETE CASCADE,
                             product_id BIGINT NOT NULL,
                             quantity INTEGER NOT NULL
);

-- Get Products Table (matches GetProduct.java)
CREATE TABLE get_product (
                             id BIGSERIAL PRIMARY KEY,
                             bxgy_coupon_id BIGINT NOT NULL REFERENCES bxgy_coupon(id) ON DELETE CASCADE,
                             product_id BIGINT NOT NULL,
                             quantity INTEGER NOT NULL
);

-- Indexes for performance (matching @Index annotations)
CREATE INDEX idx_coupon_code ON coupon(coupon_code);
CREATE INDEX idx_coupon_type ON coupon(type);
CREATE INDEX idx_coupon_active ON coupon(is_active);
CREATE INDEX idx_product_wise_product ON product_wise_coupon(product_id);
CREATE INDEX idx_buy_product_coupon ON buy_product(bxgy_coupon_id);
CREATE INDEX idx_get_product_coupon ON get_product(bxgy_coupon_id);

-- Add table comments for documentation
COMMENT ON TABLE coupon IS 'Base table for all coupon types (JOINED inheritance strategy)';
COMMENT ON TABLE cart_wise_coupon IS 'Discount applied to entire cart if threshold is met';
COMMENT ON TABLE product_wise_coupon IS 'Discount applied to specific products';
COMMENT ON TABLE bxgy_coupon IS 'Buy X Get Y type offers';
COMMENT ON TABLE buy_product IS 'Products that must be purchased for BxGy offer';
COMMENT ON TABLE get_product IS 'Products that are free/discounted in BxGy offer';
