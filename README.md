# Monk Commerce – Coupon Management API

A backend assignment implementing a modular, extensible coupon engine supporting **cart-wise**, **product-wise**, and **BxGy (“Buy X Get Y”)** discount logic.

The project is built using **Spring Boot**, with a strong focus on clean architecture, maintainability, and extensibility using the **Strategy Pattern**, allowing new coupon types to be added without modifying existing logic. The emphasis was on correctness, readability, modular design, and documenting all possible use cases, as required in the assignment.

---

# 🖼 Architecture Diagram

<img width="2345" height="1706" alt="architecture" src="https://github.com/user-attachments/assets/2a966279-5396-4560-a1e9-319c5dcf667e" />

---

# 🧱 Project Structure Overview

```
src/main/java/com/monk/commerce/task
│
├── controller        # REST controllers for Cart & Coupons
├── dto               # Request/Response DTOs
│   ├── request
│   └── response
├── entity            # JPA Entities (CartWise, ProductWise, BxGy)
├── enums             # CouponType, DiscountType
├── exception         # Custom exceptions + GlobalExceptionHandler
├── factory           # CouponStrategyFactory (core for extensibility)
├── mapper            # Convert DTO <-> Entities
├── repository        # Spring Data JPA repository
├── service           # Interfaces
│   └── serviceImpl   # Business logic implementations
├── strategy          # Strategy implementations per coupon type
├── util              # Constants + discount helpers
└── validator         # Input validations for coupon & cart
```

The structure follows clean separation of concerns and is aligned with the assignment requirements. Swagger/OpenAPI configuration is also included.

---

# 🚀 Features Implemented

## 1️⃣ CRUD Operations for Coupons

All CRUD operations required in the assignment are fully implemented:

- **POST `/coupons`** – Create coupon
- **GET `/coupons`** – Retrieve all coupons
- **GET `/coupons/{id}`** – Get coupon by ID
- **PUT `/coupons/{id}`** – Update coupon
- **DELETE `/coupons/{id}`** – Delete coupon

These include DTO–entity mapping, validation, and error handling.

---

## 2️⃣ Applicable Coupons & Coupon Application

### **POST `/cart/applicable-coupons`**
Fetches all coupons applicable to the current cart and returns expected discount amounts.

### **POST `/cart/apply-coupon/{id}`**
Applies a coupon to the cart and returns the updated cart including item-level discounts.

Both internally use the strategy engine based on coupon type.

---

## 3️⃣ Coupon Types Implemented

### ✔ Cart-wise Discount
Example: 10% off when cart total ≥ threshold.

### ✔ Product-wise Discount
Example: 20% off on Product ID = X.

### ✔ BxGy (Buy X Get Y)
Fully implemented including:

- Multiple buy products
- Multiple get products
- Combined buy quantities
- Repetition limit
- Free item additions
- Discount computed from additional quantity

**Logic Example:**

```java
if (eligibleBuyCount >= requiredBuyQty) {
    int repetitions = Math.min(eligibleBuyCount / requiredBuyQty, coupon.getRepetitionLimit());
    freeQuantity = repetitions * requiredFreeQty;
}
```

Entities involved: **BxGyCoupon**, **BuyProduct**, **GetProduct**

---

## 4️⃣ Strategy Pattern (Extensible Design)

The `CouponStrategyFactory` returns the correct strategy:

```java
switch (couponType) {
    case CART_WISE -> cartWiseCouponStrategy;
    case PRODUCT_WISE -> productWiseCouponStrategy;
    case BXGY -> bxGyCouponStrategy;
}
```

Each strategy implements:

```java
public interface CouponStrategy {
    boolean isApplicable(cart, coupon);
    double calculateDiscount(cart, coupon);
    UpdatedCartDTO apply(cart, coupon);
}
```

Adding a new coupon type requires:

- New enum in `CouponType`
- (Optional) new entity
- New strategy implementation
- Registering in `CouponStrategyFactory`

**No changes to existing business logic** → Open/Closed Principle.

---

## 5️⃣ Global Exception Handling

Custom exceptions include:

- `CouponNotFoundException`
- `InvalidCouponException`
- `InvalidCartException`
- `CouponNotApplicableException`

All handled by `GlobalExceptionHandler`, returning consistent JSON responses using `ApiErrorResponseDTO`.

---

# 🧩 Edge Cases Considered (Implemented)

✔ Product-wise coupon only applies if product exists in cart  
✔ Cart-wise threshold validated using actual total  
✔ BxGy engine supports:

- Mixed buy product arrays
- Mixed get product arrays
- Strict repetition limits
- Adding free items correctly
- Handling missing get-products

✔ DTO-level validation  
✔ Meaningful exceptions for invalid/incomplete data  
✔ Coupon rejected when:

- Cart is empty
- Quantities invalid
- Product ID missing
- Coupon type invalid

---

# ⛔ Edge Cases Not Fully Implemented

(Not required by assignment; only documentation expected.)

❌ Multi-coupon stacking  
❌ Product-level exclusions  
❌ Maximum discount cap (e.g., “20% up to ₹100”)  
❌ Tiered BxGy (buy 2 get 1, buy 4 get 3)  
❌ Per-user coupon usage limits  
❌ Coupon expiry date  
❌ Coupon priority when multiple apply

---

# 📝 Assumptions Made

- No product catalog — product info comes from cart request
- Cart is not persisted
- Free BxGy products represented by increasing quantity
- Only one coupon applied at a time
- Threshold comparisons use `>=`
- Cart items identified by productId only

---

# 📡 API Documentation (Simplified)

## **1. Create Coupon**

### `POST /api/coupons`

BxGy example:

```json
{
  "type": "bxgy",
  "details": {
    "buy_products":[{"product_id":1,"quantity":3}],
    "get_products":[{"product_id":3,"quantity":1}],
    "repetition_limit": 2
  }
}
```

---

## **2. Get Applicable Coupons**

### `POST /api/cart/applicable-coupons`

```json
{
  "cart": {
    "items": [
      {"product_id": 1, "quantity": 6, "price": 50},
      {"product_id": 3, "quantity": 2, "price": 25}
    ]
  }
}
```

---

## **3. Apply Coupon**

### `POST /api/cart/apply-coupon/{id}``

Returns:

- Updated cart
- Free items added
- Total discount
- Final price

---

# 🔧 How to Add a New Coupon Type

1. Add enum in `CouponType`
2. Create entity (if needed)
3. Create DTO
4. Implement new `CouponStrategy`
5. Register in factory
6. Update mapper

---

# ⏳ Limitations

- No authentication
- No product catalog
- No persistent cart
- No pagination
- No rate-limiting
- BxGy does not support variants or weighted items

---

# 🧭 Future Improvements

- Coupon expiry dates
- Coupon priority rules
- Maximum discount capping
- Support stacking multiple coupons
- Product catalog system
- Strategy unit tests
- Caching
- Advanced BxGy patterns

---

# 🏁 Conclusion

This project fulfills all core requirements of the Monk Commerce assignment:

- Clean modular architecture
- Strategy-based extensibility
- All coupon types implemented
- CRUD operations complete
- Proper validation & error handling
- All assumptions, edge cases, and limitations documented
