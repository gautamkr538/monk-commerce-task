# Monk Commerce – Coupon Management API

A production-ready and extensible **Coupon Management Engine** designed to support multiple discount models, including **Cart-Wise**, **Product-Wise**, and **BxGy (“Buy X Get Y”)** offers. The implementation addresses real-world business requirements such as global and per-user usage limits, coupon prioritization, stacking rules, excluded product handling, tiered BxGy configurations, and comprehensive validation.

Developed using modern **Java and Spring Boot** practices, the system emphasizes clean architecture, scalability, maintainability, and adherence to key engineering principles such as the **Strategy Pattern**, **Open/Closed Principle**, and a well-structured layered architecture.

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

## Technologies Used

| Technology | Purpose |
|-----------|----------|
| Java 21 | Core language |
| Spring Boot 3 | REST APIs, DI, Validation |
| PostgreSQL | Database |
| Hibernate/JPA | ORM & inheritance mapping |
| Lombok | Boilerplate reduction |
| Swagger/OpenAPI | API documentation |
| Maven | Build & dependency management |

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

| Type | Description | Example |
|------|-------------|---------|
| Cart-Wise | Threshold + % discount | 10% off above ₹500 |
| Product-Wise | Product-based discount | 5% off Product A |
| BxGy | Buy X Get Y free | Buy 4 A → Get 2 B |

---

### 2. Usage Tracking & Limits

| Feature          | Description                   | Example                          |
|------------------|-------------------------------|----------------------------------|
| Global Limit     | Max total coupon uses         | 100 uses total                   |
| Per-User Limit   | Max uses per user             | 1 use per user                   |
| Remaining Count  | Shows current availability    | 45 redeemed, 55 remaining        |

---

### 3. Priority System

| Feature          | Description             | Result                     |
|------------------|-------------------------|----------------------------|
| Priority Levels  | 0–N                     | Higher priority applied     |
| Sorting          | DESC order              | Highest-benefit coupon first |
| Example          | Priority 10 vs 5        | Priority 10 wins           |

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

| Operation    | Endpoint            | Notes                          |
|--------------|---------------------|--------------------------------|
| Create       | POST /coupons       | Auto-generate coupon code      |
| Get All      | GET /coupons        | Returns only active coupons    |
| Get One      | GET /coupons/{id}   | Retrieves full coupon details  |
| Update       | PUT /coupons/{id}   | Updates coupon fields          |
| Soft Delete  | DELETE /coupons/{id}| Sets is_active = false         |

---

### 10. Cart Operations

| Operation            | Description                               |
|----------------------|-------------------------------------------|
| Applicable Coupons   | Returns all eligible coupons for the cart |
| Apply Coupon         | Calculates discount & updates cart        |
| Free Items           | Injects free items (BxGy) into cart       |
| Final Amount         | Computes the final payable price          |

- Applied coupon → updated cart + free items + total discount

---

# Strategy Pattern

| Strategy               | Handles                    | Class                        |
|------------------------|----------------------------|-------------------------------|
| Cart-Wise Strategy     | Threshold & % discount     | CartWiseCouponStrategy        |
| Product-Wise Strategy  | Product-based discount     | ProductWiseCouponStrategy     |
| BxGy Strategy          | Buy X Get Y logic          | BxGyCouponStrategy            |

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

## Assumptions & Limitations

---

## Core Assumptions

```java
private static final int MAX_CART_ITEMS = 100;
private static final BigDecimal MAX_ITEM_PRICE = new BigDecimal("1000000");
private static final int MAX_ITEM_QUANTITY = 1000;
private static final BigDecimal MAX_CART_TOTAL = new BigDecimal("10000000");
```

| Assumption | Rationale |
|-----------|-----------|
| **Single coupon per cart during apply phase** | Initial design keeps apply flow simple; stacking handled later in resolver logic |
| **Cart items remain in-memory during request cycle** | No user session/cart persistence implemented |
| **Coupons sorted by priority (DESC)** | Ensures highest-benefit coupon is evaluated first |
| **Excluded products blacklist entire product** | Variant-level exclusion |
| **Discount cannot exceed cart total** | Hard safety cap to prevent negative payable amounts |
| **User ID is authenticated & trusted** | No additional validation to prevent cross-user coupon usage |
| **BxGy tiered logic always gives max benefit** | System automatically selects the highest qualifying tier |

---

## Functional Limitations

| Limitation | Impact |
|-----------|--------|
| **No multi-coupon conflict prevention** | Incorrectly marked stackable coupons may get combined in future stacking logic |
| **No sequential validation for stacking** | Complex order-based stacking scenarios not enforced |
| **Scheduled activation (`start_date`) not supported** | Coupons become active immediately if `is_active = true` |
| **No user segmentation (VIP, loyalty tiers)** | All coupons available to all users unless explicitly restricted |
| **Category-based or brand-based coupons not available** | Coupons apply only to product IDs or entire cart |

## Test Coverage & Execution Report

- All test classes for every component in the project have been fully implemented.

- View the complete test coverage and execution output here: **[View Test Report](https://raw.githubusercontent.com/gautamkr538/monk-commerce-task/main/src/main/resources/test_report.html)**  

