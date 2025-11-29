CREATE TABLE coupon (id UUID PRIMARY KEY DEFAULT gen_random_uuid(),coupon_code VARCHAR(50) UNIQUE NOT NULL,
type VARCHAR(20) NOT NULL,description VARCHAR(500),
is_active BOOLEAN NOT NULL DEFAULT TRUE,
expiration_date TIMESTAMP,usage_count BIGINT NOT NULL DEFAULT 0,max_usage_limit BIGINT,usage_limit_per_user INTEGER,
allow_stacking BOOLEAN NOT NULL DEFAULT FALSE,priority INTEGER NOT NULL DEFAULT 0,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);

CREATE TABLE cart_wise_coupon (id UUID PRIMARY KEY REFERENCES coupon(id) ON DELETE CASCADE,threshold_amount DECIMAL(10,2) NOT NULL,
discount_percentage DECIMAL(5,2) NOT NULL,max_discount_amount DECIMAL(10,2));


CREATE TABLE product_wise_coupon (id UUID PRIMARY KEY REFERENCES coupon(id) ON DELETE CASCADE,product_id BIGINT NOT NULL,
discount_percentage DECIMAL(5,2) NOT NULL,max_discount_per_product DECIMAL(10,2));


CREATE TABLE bxgy_coupon (id UUID PRIMARY KEY REFERENCES coupon(id) ON DELETE CASCADE,
repetition_limit INTEGER NOT NULL, is_tiered BOOLEAN NOT NULL DEFAULT FALSE);


CREATE TABLE buy_product (id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
bxgy_coupon_id UUID NOT NULL REFERENCES bxgy_coupon(id) ON DELETE CASCADE,
product_id BIGINT NOT NULL, quantity INTEGER NOT NULL,
tier_level INTEGER NOT NULL DEFAULT 1);



CREATE TABLE get_product (id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
bxgy_coupon_id UUID NOT NULL REFERENCES bxgy_coupon(id) ON DELETE CASCADE,
product_id BIGINT NOT NULL,quantity INTEGER NOT NULL,tier_level INTEGER NOT NULL DEFAULT 1);


CREATE TABLE excluded_product (id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
coupon_id UUID NOT NULL REFERENCES coupon(id) ON DELETE CASCADE,product_id BIGINT NOT NULL,UNIQUE(coupon_id, product_id));

CREATE TABLE coupon_usage (id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
coupon_id UUID NOT NULL REFERENCES coupon(id) ON DELETE CASCADE,
user_id VARCHAR(100) NOT NULL,usage_count INTEGER NOT NULL DEFAULT 1,
last_used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE(coupon_id, user_id));

CREATE INDEX idx_coupon_code ON coupon(coupon_code);
CREATE INDEX idx_coupon_type ON coupon(type);
CREATE INDEX idx_coupon_active ON coupon(is_active);
CREATE INDEX idx_coupon_expiration ON coupon(expiration_date);
CREATE INDEX idx_coupon_priority ON coupon(priority);
CREATE INDEX idx_product_wise_product ON product_wise_coupon(product_id);
CREATE INDEX idx_buy_product_coupon ON buy_product(bxgy_coupon_id);
CREATE INDEX idx_buy_product_tier ON buy_product(tier_level);
CREATE INDEX idx_get_product_coupon ON get_product(bxgy_coupon_id);
CREATE INDEX idx_get_product_tier ON get_product(tier_level);
CREATE INDEX idx_excluded_product_coupon ON excluded_product(coupon_id);
CREATE INDEX idx_excluded_product_product ON excluded_product(product_id);
CREATE INDEX idx_coupon_usage_user ON coupon_usage(user_id);
CREATE INDEX idx_coupon_usage_coupon ON coupon_usage(coupon_id);
