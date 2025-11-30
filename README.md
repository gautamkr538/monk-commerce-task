# Monk Commerce – Coupon Management API

A production-ready and extensible **Coupon Management Engine** designed to support multiple discount models, including **Cart-Wise**, **Product-Wise**, and **BxGy (“Buy X Get Y”)** offers. The implementation addresses real-world business requirements such as global and per-user usage limits, coupon prioritization, stacking rules, excluded product handling, tiered BxGy configurations, and comprehensive validation.

Developed using modern **Java and Spring Boot** practices, the system emphasizes clean architecture, scalability, maintainability, and adherence to key engineering principles such as the **Strategy Pattern**, **Open/Closed Principle**, and a well-structured layered architecture.

**Technology Stack:**  
**Java 21 · Spring Boot 3 · PostgreSQL · Hibernate/JPA · Swagger/OpenAPI · Lombok · Jakarta Validation · Maven**

---

# Architecture Diagram

<img width="3090" height="1706" alt="architecture" src="https://github.com/user-attachments/assets/2a07e155-17cd-464d-80b4-e91c35eb85a3" />

---

# Project Structure Overview

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

# Technologies Used

- **Java 21**
- **Spring Boot 3 (REST + DI + Validation)**
- **PostgreSQL**
- **Hibernate/JPA with JOINED Inheritance**
- **Lombok**
- **Swagger / OpenAPI**
- **Maven**

---

# Database Schema (JOINED Inheritance)

<img width="3820" height="2503" alt="schema_architecture" src="https://github.com/user-attachments/assets/43c2dd62-2737-4912-80f1-c4ba98d543fa" />

The coupon engine uses **JOINED strategy** to map:

- `coupon` → base table  
- `cart_wise_coupon` → derived table
- `product_wise_coupon` → derived table
- `bxgy_coupon` → derived table
- `buy_product` → child of bxgy_coupon (buy items per tier)  
- `get_product` → child of bxgy_coupon (free items per tier)  
- `excluded_product` → products excluded from a coupon
- `coupon_usage` → tracks usage per user

Indexes added for performance and optimized search.

The full PostgreSQL schema used by the coupon engine: **[View Schema.sql](https://raw.githubusercontent.com/gautamkr538/monk-commerce-task/main/src/main/resources/schema.sql)**

---

## Features Implemented

### 1. Core Coupon Types
- **Cart-Wise** (threshold + discount + max cap)
- **Product-Wise** (specific product discount + max per product cap)
- **BxGy** (multi-buy, multi-get, repetition limit, tier support)

**Example: – Limited Flash Sale**
- `max_usage_limit: 100`
- `usage_limit_per_user: 1`
- Output: **“45 redeemed, 55 remaining globally”**

**Example: – Tiered BxGy**
- Buy 2 A → Get 1 B free  
- Buy 4 A → Get 2 B free  
- `repetition_limit: 2`  
- Output: **“Cart: 8 × A qualifies for 4 × B free”**

---

### 2. Usage Tracking & Limits
- Global usage limit (`max_usage_limit`)
- Per-user usage limit (`usage_limit_per_user`)
- Track total usage & user-specific usage
- Show remaining uses for both

**Example: – Loyalty Reward Coupon**
- Unlimited global usage  
- Per-user limit: **5**  
- Output: **“User has 3 uses remaining”**

---

### 3. Priority System
- Each coupon has **priority (0–N)**
- Coupons sorted by priority (DESC)
- Highest priority coupon selected
- Combined with discount calculation for best match

**Example:**
- Coupon A → 10% off (priority: 10)
- Coupon B → 5% off (priority: 5)
- System selects **Coupon A**

---

### 4. Stacking (Metadata Level)
- `is_stackable` flag stored
- Returned in API responses
- Future-ready for multi-coupon stacking

**Example:**
- Coupon A (`is_stackable: true`)
- Coupon B (`is_stackable: true`)
- Future system can apply **both simultaneously**

---

### 5. Excluded Products
- Configurable exclusion list
- Coupon automatically rejected if excluded product is in cart

**Example:**
- Exclude: `Product X, Product Y`  
- If cart contains X → **coupon invalid**

---

### 6. Tiered BxGy (Multi-Level Buy X Get Y)
- Multiple **tier levels**
- Auto-detect best tier
- Enforce repetition limit
- Optimized free item calculation

**Example:**
- Cart: **8 × Product A**  
- Tier: Buy 4 A → Get 2 B  
- Applied twice → **4 × Product B free**

---

### 7. Discount Calculation Engine
Using **Strategy Pattern**, supports:
- Percentage-based discounts
- Max discount caps
- Per-product discount caps
- Free item discounts (BxGy)
- Threshold validation
- Product presence checks
- Multi-tier BxGy validation

**Example:**
- BxGy → validates buy & get quantity  
- Cart-wise → validates threshold + cap

---

### 8. Validation & Error Handling
Handled via validators & exception handler:
- Empty cart
- Invalid quantity/price
- Duplicate products
- Expired/inactive coupon
- Invalid coupon ID
- Threshold not met
- Product not present (product-wise)
- Insufficient buy quantity (BxGy)

**Example:**
- User applies expired coupon → Error  
- User exceeds usage limit → Error  

---

### 9. CRUD Operations
- Create coupon (auto-generates code if absent)
- Retrieve active coupons
- Get coupon by ID
- Update coupon
- Soft delete (mark `is_active=false`)

**Example:**
- Coupon deletion → never hard deleted, only soft-removed

---

### 10. Cart Operations
- Fetch applicable coupons
- Apply coupon to cart
- Compute final payable amount
- Add free items for BxGy
- Return detailed discount breakdown

**Example:**
- Applied coupon → updated cart + free items + total discount

---

# Strategy Pattern

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

# API Documentation (via Swagger)

Swagger UI is auto-generated at:

- **http://localhost:8080/monk/swagger-ui/index.html#/**

All endpoints, payloads, and responses can be tested directly in-browser.

---

# Scenario Coverage Postman Collection

A complete Postman collection is included for testing all coupon **scenario coverage**: **[View Postman Collection](https://raw.githubusercontent.com/gautamkr538/monk-commerce-task/main/src/main/resources/postman_collection.json)**

---

# Complete Scenario Coverage Summary

### Fully Implemented Areas

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

### Important Future Enhancements

#### 1. Multi-Coupon Stacking Engine
Apply multiple eligible coupons at once:
- Apply in priority order
- Combine discounts carefully
- Prevent conflicts with non-stackable coupons

#### 2. Schedule-Based Coupons
- Time-of-day restrictions (happy hours)
- Day-of-week rules

#### 3. User Segmentation
- First-order coupons
- Loyalty tier coupons

#### 4. Category & Brand Based Coupons
- Requires product catalog integration

#### 5. Inventory-Dependent Coupons
- Stock-based limits

#### 6. Mapping Optimization (Dozer/MapStruct)
We can eliminate mapping boilerplate using:
- **Dozer**
- **MapStruct**
- Or a custom **DozerUtil**

This will make DTO ↔ Entity mapping maintainable & cleaner.
