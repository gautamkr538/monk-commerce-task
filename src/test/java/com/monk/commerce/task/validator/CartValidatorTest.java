package com.monk.commerce.task.validator;

import com.monk.commerce.task.dto.request.CartItemDTO;
import com.monk.commerce.task.dto.request.CartRequestDTO;
import com.monk.commerce.task.exception.InvalidCartException;
import com.monk.commerce.task.util.Constants;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CartValidatorTest {

    private final CartValidator validator = new CartValidator();

    private CartItemDTO createItem(Long productId, Integer quantity, BigDecimal price) {
        CartItemDTO item = new CartItemDTO();
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setPrice(price);
        return item;
    }

    private CartRequestDTO createRequest(List<CartItemDTO> items) {
        CartRequestDTO request = new CartRequestDTO();
        request.setItems(items);
        return request;
    }

    @Test
    void validateCartRequest_nullCart_throwsInvalidCartException() {
        InvalidCartException ex = assertThrows(
                InvalidCartException.class,
                () -> validator.validateCartRequest(null)
        );
        assertEquals(Constants.INVALID_CART, ex.getMessage());
    }

    @Test
    void validateCartRequest_emptyItems_throwsInvalidCartException() {
        CartRequestDTO request = createRequest(new ArrayList<>());

        InvalidCartException ex = assertThrows(
                InvalidCartException.class,
                () -> validator.validateCartRequest(request)
        );
        assertEquals(Constants.EMPTY_CART, ex.getMessage());
    }

    @Test
    void validateCartRequest_moreThanMaxItems_throwsInvalidCartException() {
        List<CartItemDTO> items = new ArrayList<>();
        // MAX_CART_ITEMS = 100 → create 101 items
        for (int i = 1; i <= 101; i++) {
            items.add(createItem((long) i, 1, BigDecimal.TEN));
        }

        CartRequestDTO request = createRequest(items);

        InvalidCartException ex = assertThrows(
                InvalidCartException.class,
                () -> validator.validateCartRequest(request)
        );
        assertTrue(ex.getMessage().contains("Cart cannot contain more than"));
    }

    @Test
    void validateCartRequest_duplicateProduct_throwsInvalidCartException() {
        List<CartItemDTO> items = new ArrayList<>();
        items.add(createItem(1L, 1, BigDecimal.TEN));
        items.add(createItem(1L, 2, BigDecimal.valueOf(20)));

        CartRequestDTO request = createRequest(items);

        InvalidCartException ex = assertThrows(
                InvalidCartException.class,
                () -> validator.validateCartRequest(request)
        );
        assertTrue(ex.getMessage().contains("Duplicate product in cart"));
    }

    @Test
    void validateCartRequest_validCart_doesNotThrow() {
        List<CartItemDTO> items = new ArrayList<>();
        items.add(createItem(1L, 2, BigDecimal.valueOf(100.50)));
        items.add(createItem(2L, 1, BigDecimal.valueOf(200.00)));

        CartRequestDTO request = createRequest(items);

        assertDoesNotThrow(() -> validator.validateCartRequest(request));
    }

    @Test
    void validateCartItem_nullItem_throwsInvalidCartException() {
        InvalidCartException ex = assertThrows(
                InvalidCartException.class,
                () -> validator.validateCartItem(null)
        );
        assertEquals("Cart item cannot be null", ex.getMessage());
    }

    @Test
    void validateCartItem_invalidProductId_throwsInvalidCartException() {
        CartItemDTO item = createItem(0L, 1, BigDecimal.TEN);

        InvalidCartException ex = assertThrows(
                InvalidCartException.class,
                () -> validator.validateCartItem(item)
        );
        assertEquals("Invalid product ID", ex.getMessage());
    }

    @Test
    void validateCartItem_nullQuantity_throwsInvalidCartException() {
        CartItemDTO item = createItem(1L, null, BigDecimal.TEN);

        InvalidCartException ex = assertThrows(
                InvalidCartException.class,
                () -> validator.validateCartItem(item)
        );
        assertTrue(ex.getMessage().contains("Invalid quantity"));
    }

    @Test
    void validateCartItem_zeroQuantity_throwsInvalidCartException() {
        CartItemDTO item = createItem(1L, 0, BigDecimal.TEN);

        InvalidCartException ex = assertThrows(
                InvalidCartException.class,
                () -> validator.validateCartItem(item)
        );
        assertTrue(ex.getMessage().contains("Invalid quantity"));
    }

    @Test
    void validateCartItem_quantityExceedsMax_throwsInvalidCartException() {
        // MAX_ITEM_QUANTITY = 1000 → use 1001
        CartItemDTO item = createItem(1L, 1001, BigDecimal.TEN);

        InvalidCartException ex = assertThrows(
                InvalidCartException.class,
                () -> validator.validateCartItem(item)
        );
        assertTrue(ex.getMessage().contains("Quantity exceeds limit"));
    }

    @Test
    void validateCartItem_nullPrice_throwsInvalidCartException() {
        CartItemDTO item = createItem(1L, 1, null);

        InvalidCartException ex = assertThrows(
                InvalidCartException.class,
                () -> validator.validateCartItem(item)
        );
        assertTrue(ex.getMessage().contains("Price cannot be null"));
    }

    @Test
    void validateCartItem_negativePrice_throwsInvalidCartException() {
        CartItemDTO item = createItem(1L, 1, BigDecimal.valueOf(-10));

        InvalidCartException ex = assertThrows(
                InvalidCartException.class,
                () -> validator.validateCartItem(item)
        );
        assertTrue(ex.getMessage().contains("Price cannot be negative"));
    }

    @Test
    void validateCartItem_priceExceedsMax_throwsInvalidCartException() {
        // MAX_ITEM_PRICE = 1_000_000 → use 1_000_001
        CartItemDTO item = createItem(1L, 1, new BigDecimal("1000001"));

        InvalidCartException ex = assertThrows(
                InvalidCartException.class,
                () -> validator.validateCartItem(item)
        );
        assertTrue(ex.getMessage().contains("Price exceeds limit"));
    }

    @Test
    void validateCartItem_priceWithMoreThanTwoDecimals_throwsInvalidCartException() {
        CartItemDTO item = createItem(1L, 1, new BigDecimal("10.123"));

        InvalidCartException ex = assertThrows(
                InvalidCartException.class,
                () -> validator.validateCartItem(item)
        );
        assertTrue(ex.getMessage().contains("Price can have max 2 decimal places"));
    }

    @Test
    void validateCartItem_validItem_doesNotThrow() {
        CartItemDTO item = createItem(1L, 5, new BigDecimal("999999.99"));

        assertDoesNotThrow(() -> validator.validateCartItem(item));
    }

    @Test
    void validateCartTotal_negativeTotal_throwsInvalidCartException() {
        List<CartItemDTO> items = new ArrayList<>();
        items.add(createItem(1L, 1, new BigDecimal("-10.00")));

        CartRequestDTO request = createRequest(items);

        InvalidCartException ex = assertThrows(
                InvalidCartException.class,
                () -> validator.validateCartTotal(request)
        );
        assertEquals("Cart total cannot be negative", ex.getMessage());
    }

    @Test
    void validateCartTotal_exceedsMaxTotal_throwsInvalidCartException() {
        // MAX_CART_TOTAL = 10_000_000 → exceed it
        List<CartItemDTO> items = new ArrayList<>();
        items.add(createItem(1L, 1, new BigDecimal("9000000")));
        items.add(createItem(2L, 1, new BigDecimal("2000000"))); // total = 11_000_000

        CartRequestDTO request = createRequest(items);

        InvalidCartException ex = assertThrows(
                InvalidCartException.class,
                () -> validator.validateCartTotal(request)
        );
        assertTrue(ex.getMessage().contains("Cart total exceeds"));
    }

    @Test
    void validateCartTotal_validTotal_doesNotThrow() {
        List<CartItemDTO> items = new ArrayList<>();
        items.add(createItem(1L, 2, new BigDecimal("1000000"))); // 2_000_000
        items.add(createItem(2L, 3, new BigDecimal("500000")));  // 1_500_000
        // total = 3_500_000 < 10_000_000

        CartRequestDTO request = createRequest(items);

        assertDoesNotThrow(() -> validator.validateCartTotal(request));
    }
}
