# Monk Commerce – Coupon Management API

A backend assignment implementing a fully extensible **Coupon Engine** supporting **Cart-Wise**, **Product-Wise**, and **BxGy (“Buy X Get Y”)** discounts with real-world scenarios like usage limits, priority system, stacking flags, excluded products, tiered BxGy, and more.

Technology Stack: **Java 21**, **Spring Boot 3**, **PostgreSQL**, **Hibernate**, **Swagger**, **Lombok**, **Jakarta Validation**, **Maven**.

---

# 🖼 Architecture Diagram
(Architecture PNG available in repository)

<img width="2345" height="1706" alt="architecture" src="https://github.com/user-attachments/assets/7bf0ded3-f631-4875-a10b-936dd413beb0" />

---

# 🧱 Project Structure Overview

```
src/main/java/com/monk/commerce/task
│
├── controller        # REST controllers for Cart & Coupons
├── dto               # Request/Response DTOs
├── entity            # JPA Entities (JOINED inheritance for coupons)
├── enums             # CouponType, DiscountType
├── exception         # Custom exceptions + GlobalExceptionHandler
├── factory           # CouponStrategyFactory (returns strategy instance)
├── mapper            # DTO ↔ Entity mapping
├── repository        # JPA repositories
├── service           # Interfaces
│   └── serviceImpl   # Implementations with business logic
├── strategy          # Individual strategies for each coupon type
├── util              # Reusable utilities (DiscountCalculator, CouponUtil)
└── validator         # Input validations
```

---

# ⚙️ Technologies Used

- **Java 21**
- **Spring Boot 3 (REST + DI + Validation)**
- **PostgreSQL**
- **Hibernate/JPA with JOINED Inheritance**
- **Lombok**
- **Swagger / OpenAPI**
- **Maven**

---

# 🗂 Database Schema (JOINED Inheritance)

![Schema Diagram](schema.png)

The coupon engine uses **JOINED strategy** to map:

- `coupon` → base table
- `cart_wise_coupon`
- `product_wise_coupon`
- `bxgy_coupon`
- `buy_product`
- `get_product`

Indexes added for performance and optimized search.

---

# 🚀 Features Implemented (Production-Level)

## ✅ 1. Core Coupon Types
- **Cart-Wise** (threshold + discount + max cap)
- **Product-Wise** (specific product discount + max per product cap)
- **BxGy** (multi-buy, multi-get, repetition limit, tier support)

---

## ✅ 2. Usage Tracking & Limits
Fully implemented:

- Global usage limit (`max_usage_limit`)
- Per-user usage limit (`usage_limit_per_user`)
- Track total usage & user-specific usage
- Show remaining uses for both

---

## ✅ 3. Priority System
- Each coupon has a **priority (0–N)**
- Applicable coupons sorted by priority **DESC**
- Higher priority coupons shown first
- Combined with discount calculation to identify the **best coupon**

---

## ✅ 4. Stacking (Metadata Level)
- `is_stackable` boolean flag implemented
- Returned in API responses
- Controls future stacking rules  
  (*Full multi-coupon stacking will come in future phase*)

---

## ✅ 5. Excluded Products
- Configurable excluded product list
- Coupon is rejected if excluded product appears in cart
- Ensures realistic exclusion logic (premium items, special SKUs)

---

## ✅ 6. Tiered BxGy (Multi-Level Buy X Get Y)
- Supports **tier_level 1, 2, 3...**
- Auto-detect best applicable tier
- Repetition limit enforced
- Free item calculation optimized
- Discount calculation based on tier rules

---

## ✅ 7. Discount Calculation Engine
Built with Strategy Pattern:

- Percentage discounts
- Max-discount caps
- Per-product discount cap
- Free item discount logic (BxGy)
- Cart threshold validations
- Product presence validations
- BxGy buy quantity & get quantity validations

---

## ✅ 8. Validation & Error Handling
Handled through validators + exception handler:

- Empty cart
- Negative values
- Duplicate products
- Expired coupon
- Inactive coupon
- Invalid ID
- Below threshold
- Missing product for product-wise
- Insufficient buy quantity for BxGy

All mapped to uniform error response DTO.

---

## ✅ 9. CRUD Operations
- Create Coupon (auto-generate coupon code if not provided)
- Get all active coupons
- Get coupon by ID
- Update coupon
- Soft delete coupon via `is_active` flag

---

## ✅ 10. Cart Operations
- Fetch applicable coupons
- Apply a coupon to cart
- Compute final payable amount
- Insert free items for BxGy
- Final response contains per-item + total discount

---

# 🧠 Strategy Pattern (Core Engine)

Each coupon type has its own strategy:

```java
public interface CouponStrategy {
    boolean isApplicable(CartRequestDTO cart, Coupon coupon);
    double calculateDiscount(CartRequestDTO cart, Coupon coupon);
    UpdatedCartDTO apply(CartRequestDTO cart, Coupon coupon);
}
```

Factory resolves correct strategy:

```java
switch (couponType) {
    case CART_WISE -> cartWiseCouponStrategy;
    case PRODUCT_WISE -> productWiseCouponStrategy;
    case BXGY -> bxGyCouponStrategy;
}
```

Ensures OCP (Open-Closed Principle) → new coupon types require **zero modification** to existing logic.

---

# 🧾 API Documentation (via Swagger)

Swagger UI is auto-generated at:

👉 **http://localhost:8080/monk/swagger-ui/index.html#/**

All endpoints, payloads, and responses can be tested directly in-browser.

---

# 📊 Scenario Coverage (Detailed Matrix)

The full **scenario coverage table** (100+ scenarios) is included inside this README below.

---

# 📈 Complete Scenario Coverage Summary

(A compressed but detailed version)

### ✅ Fully Implemented Areas
1. Cart-wise coupons
2. Product-wise coupons
3. BxGy + Tiered BxGy
4. Usage limits (global + per user)
5. Tracking counters
6. Priority system
7. Stacking flag (metadata)
8. Excluded products
9. All major validation cases
10. CRUD & soft delete
11. Final price calculation
12. Strategy-based architecture

### 🔮 Important Future Enhancements

#### 1. Multi-Coupon Stacking Engine
Apply multiple eligible coupons at once:
- Apply in priority order
- Combine discounts carefully
- Prevent conflicts with non-stackable coupons

#### 2. Schedule-Based Coupons
- Start date
- End date
- Time-of-day restrictions (happy hours)
- Day-of-week rules

#### 3. User Segmentation
- VIP-only coupons
- First-order coupons
- Loyalty tier coupons
- Region-specific coupons

#### 4. Category & Brand Based Coupons
Requires product catalog integration

#### 5. Inventory-Dependent Coupons
(Stock-based limits)

#### 6. Analytics & Reporting
- Revenue impact
- Popularity metrics
- Export usage

#### 7. Mapping Optimization (Dozer/MapStruct)
We can eliminate mapping boilerplate using:

- **Dozer**
- **MapStruct**
- Or a custom **DozerUtil**

This will make DTO ↔ Entity mapping maintainable & cleaner.

---

# 📬 Postman Collection
Will be added soon once the shareable link is ready.

---

# 🏁 Conclusion

This project implements a robust, real-world **Coupon Engine** with:

- Clean architecture
- Strategy-based extensibility
- Rich business features
- Complete scenario coverage
- Strong validation & error handling

The codebase is engineered to scale into a production-ready promotional system.
