package com.monk.commerce.task.validator;

import com.monk.commerce.task.dto.request.CartItemDTO;
import com.monk.commerce.task.dto.request.CartRequestDTO;
import com.monk.commerce.task.exception.InvalidCartException;
import com.monk.commerce.task.util.Constants;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Component
public class CartValidator {

    /**
     * Validate cart request
     */
    public void validateCartRequest(CartRequestDTO cartRequest) {
        if (Objects.isNull(cartRequest)) {
            throw new InvalidCartException(Constants.INVALID_CART);
        }

        List<CartItemDTO> items = cartRequest.getItems();
        if (Objects.isNull(items) || items.isEmpty()) {
            throw new InvalidCartException(Constants.EMPTY_CART);
        }
        // Validate each cart item
        items.forEach(this::validateCartItem);
    }

    /**
     * Validate individual cart item
     */
    public void validateCartItem(CartItemDTO item) {
        if (Objects.isNull(item)) {
            throw new InvalidCartException("Cart item cannot be null");
        }

        if (Objects.isNull(item.getProductId()) || item.getProductId() <= 0) {
            throw new InvalidCartException("Invalid product ID");
        }

        if (Objects.isNull(item.getQuantity()) || item.getQuantity() <= 0) {
            throw new InvalidCartException("Quantity must be positive");
        }

        if (Objects.isNull(item.getPrice()) || item.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidCartException("Price must be non-negative");
        }
    }
}
