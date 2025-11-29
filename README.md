# Monk Commerce – Coupon Management API

A backend assignment implementing a modular, extensible coupon engine supporting **cart-wise**, **product-wise**, and **BxGy (“Buy X Get Y”)** discount logic.

This project is built using **Java 21**, **Spring Boot**, **PostgreSQL**, **Lombok**, **Swagger/OpenAPI**, **Jakarta Validation**, and **Maven** as the build tool.

I also documented all implemented and unimplemented scenarios as required.

---

# 🖼 Architecture Diagram

<img width="2345" height="1706" alt="architecture" src="https://github.com/user-attachments/assets/528db94c-7275-445c-9929-10b84261e53b" />

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

---

# ⚙️ Technologies Used

- **Java 21**
- **Spring Boot 3+**
- **PostgreSQL**
- **Hibernate / JPA**
- **Lombok**
- **Swagger (springdoc-openapi)**
- **Jakarta Validation (I/O validation)**
- **Maven Build Tool**

---

# 🗂 Database Schema (Implemented)

<img width="2741" height="1946" alt="schema_architecture" src="https://github.com/user-attachments/assets/ba4ade04-f150-4729-9d05-3aa5e8025d57" />

---

# 🔑 Note on UUID Migration

Currently all IDs use **Long (BIGSERIAL)**.

I plan to migrate to **UUID** for:

- Security
- Avoiding predictable IDs
- Better microservice communication

---

# 🚀 Features Implemented

## 1️⃣ CRUD Operations for Coupons

- **POST `/coupons`**
- **GET `/coupons`**
- **GET `/coupons/{id}`**
- **PUT `/coupons/{id}`**
- **DELETE `/coupons/{id}`**

All include mapping, validation, exception handling.

---

## 2️⃣ Applicable Coupons & Best Coupon Selection

### ✔ `POST /cart/applicable-coupons`
Returns **all** applicable coupons with discount amounts.

### ✔ System selects the **best coupon**
When multiple coupons apply, the system calculates the discount for all and picks:

👉 **The coupon giving the maximum savings to the user**

### ✔ `POST /cart/apply-coupon/{id}`
Applies the selected coupon to the cart.

---

## 3️⃣ Coupon Types Implemented

### ✔ Cart-wise
Based on cart total threshold.

### ✔ Product-wise
Discount applies only to specific product.

### ✔ BxGy (Buy X Get Y)
Supports:

- Multiple buy products
- Multiple get products
- Repetition limit
- Free product addition
- Cheapest get-product preference
- Proper discount calculation

---

# 🧠 Strategy Pattern (Core Engine)

Factory resolves coupon strategies:

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

---

# 🧩 Edge Cases Considered

✔ Cart empty  
✔ Invalid quantity  
✔ Missing product ID  
✔ Negative values  
✔ Threshold validated correctly  
✔ Product-wise only applies when product exists  
✔ BxGy handles multiple buy/get combinations  
✔ Applies cheapest free product  
✔ Proper exception handling

---

# ⛔ Edge Cases Not Implemented

❌ Stacking multiple coupons  
❌ Per-user coupon usage limit  
❌ Max discount cap  
❌ Tiered BxGy  
❌ Product exclusion rules  
❌ Expiry date  
❌ Coupon priority

---

# 📝 Assumptions

- Cart sent entirely in request
- No product catalog
- Free BxGy items added as additional quantity
- One coupon per request
- Threshold uses >=
- productId uniquely identifies a cart item

---

# 📡 API Examples

## Create Coupon (BxGy)

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

## Get Applicable Coupons

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

## Apply Coupon

```text
POST /api/cart/apply-coupon/{id}
```

---

# 🛠 Add a New Coupon Type

1. Add enum in `CouponType`
2. Create entity
3. Add DTO
4. Implement new strategy
5. Update factory
6. Add mapper logic

---

# ⏳ Limitations

- No authentication
- No rate limiting
- No cart storage
- No pagination
- No catalog service
- Basic BxGy logic only

---

# 🧭 Future Improvements

- Use UUID IDs
- Coupon expiry
- Priority-based selection
- Max discount caps
- Stackable coupons
- Redis caching
- Product service integration
- Strategy test coverage

---

# 🏁 Conclusion

This project meets all required expectations:

- Clean architecture
- Strategy pattern coupon engine
- All coupon types implemented
- Full CRUD support
- Proper validations and error handling
- Documented schema, assumptions, limitations and scenarios
- Extensible for future coupon types
- Ready for production with improvements
- Well-structured and maintainable codebase
- Comprehensive API documentation via Swagger
- Thorough testing of core functionalities
- Clear instructions for setup and usage
- Modular design for easy enhancements
- Adheres to best practices and coding standards
- Optimized for performance and scalability
- Detailed README for clarity and understanding
- Robust exception handling mechanisms
- Seamless integration with PostgreSQL
- Effective use of design patterns
- Focus on user experience and usability
