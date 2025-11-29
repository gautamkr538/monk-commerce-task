package com.monk.commerce.task.validator;

import com.monk.commerce.task.dto.request.CartItemDTO;
import com.monk.commerce.task.dto.request.CartRequestDTO;
import com.monk.commerce.task.exception.InvalidCartException;
import com.monk.commerce.task.util.Constants;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CartValidator {

    private static final int MAX_CART_ITEMS = 100;
    private static final BigDecimal MAX_ITEM_PRICE = new BigDecimal("1000000");
    private static final int MAX_ITEM_QUANTITY = 1000;
    private static final BigDecimal MAX_CART_TOTAL = new BigDecimal("10000000");

    public void validateCartRequest(CartRequestDTO cartRequest) {
        if (cartRequest == null) {
            throw new InvalidCartException(Constants.INVALID_CART);
        }

        List<CartItemDTO> items = cartRequest.getItems();
        if (items == null || items.isEmpty()) {
            throw new InvalidCartException(Constants.EMPTY_CART);
        }

        if (items.size() > MAX_CART_ITEMS) {
            throw new InvalidCartException("Cart cannot contain more than " + MAX_CART_ITEMS + " items");
        }

        Set<Long> productIds = new HashSet<>();
        for (CartItemDTO item : items) {
            if (!productIds.add(item.getProductId())) {
                throw new InvalidCartException("Duplicate product in cart: " + item.getProductId());
            }
        }

        items.forEach(this::validateCartItem);
        validateCartTotal(cartRequest);
    }

    public void validateCartItem(CartItemDTO item) {
        if (item == null) {
            throw new InvalidCartException("Cart item cannot be null");
        }

        if (item.getProductId() == null || item.getProductId() <= 0) {
            throw new InvalidCartException("Invalid product ID");
        }

        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new InvalidCartException("Invalid quantity for product " + item.getProductId());
        }

        if (item.getQuantity() > MAX_ITEM_QUANTITY) {
            throw new InvalidCartException("Quantity exceeds limit for product " + item.getProductId());
        }

        if (item.getPrice() == null) {
            throw new InvalidCartException("Price cannot be null for product " + item.getProductId());
        }

        if (item.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidCartException("Price cannot be negative for product " + item.getProductId());
        }

        if (item.getPrice().compareTo(MAX_ITEM_PRICE) > 0) {
            throw new InvalidCartException("Price exceeds limit for product " + item.getProductId());
        }

        if (item.getPrice().scale() > 2) {
            throw new InvalidCartException("Price can have max 2 decimal places for product " + item.getProductId());
        }
    }

    public void validateCartTotal(CartRequestDTO cartRequest) {
        BigDecimal cartTotal = cartRequest.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (cartTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidCartException("Cart total cannot be negative");
        }

        if (cartTotal.compareTo(MAX_CART_TOTAL) > 0) {
            throw new InvalidCartException("Cart total exceeds maximum allowed value");
        }
    }
}
