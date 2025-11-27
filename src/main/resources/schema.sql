-- Base Coupon Table
CREATE TABLE coupon (id BIGSERIAL PRIMARY KEY,
coupon_code VARCHAR(50) UNIQUE NOT NULL,
type VARCHAR(20) NOT NULL, description VARCHAR(500), is_active BOOLEAN DEFAULT TRUE,
expiration_date TIMESTAMP,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);

-- Cart-Wise Coupon
CREATE TABLE cart_wise_coupon (id BIGINT PRIMARY KEY REFERENCES coupon(id),
threshold_amount DECIMAL(10,2) NOT NULL,
discount_percentage DECIMAL(5,2), discount_amount DECIMAL(10,2),
max_discount_amount DECIMAL(10,2));

-- Product-Wise Coupon
CREATE TABLE product_wise_coupon (id BIGINT PRIMARY KEY REFERENCES coupon(id),
product_id BIGINT NOT NULL,
discount_percentage DECIMAL(5,2), discount_amount DECIMAL(10,2), max_discount_per_product DECIMAL(10,2));

-- BxGy Coupon
CREATE TABLE bxgy_coupon (id BIGINT PRIMARY KEY REFERENCES coupon(id),
buy_quantity INTEGER NOT NULL, get_quantity INTEGER NOT NULL, repetition_limit INTEGER NOT NULL);

-- Buy Products (Many-to-One with BxGy)
CREATE TABLE buy_product (id BIGSERIAL PRIMARY KEY,
bxgy_coupon_id BIGINT REFERENCES bxgy_coupon(id),
product_id BIGINT NOT NULL, quantity INTEGER NOT NULL);

-- Get Products (Many-to-One with BxGy)
CREATE TABLE get_product (id BIGSERIAL PRIMARY KEY, bxgy_coupon_id BIGINT REFERENCES bxgy_coupon(id),
                          product_id BIGINT NOT NULL,quantity INTEGER NOT NULL);

-- Indexes
CREATE INDEX idx_coupon_type ON coupon(type);
CREATE INDEX idx_coupon_active ON coupon(is_active);
CREATE INDEX idx_product_wise_product ON product_wise_coupon(product_id);
CREATE INDEX idx_buy_product_coupon ON buy_product(bxgy_coupon_id);
CREATE INDEX idx_get_product_coupon ON get_product(bxgy_coupon_id);
