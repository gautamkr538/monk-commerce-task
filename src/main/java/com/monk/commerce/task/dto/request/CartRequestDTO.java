package com.monk.commerce.task.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartRequestDTO {

    // List of cart items with validation to ensure it's not empty

    @Valid
    @NotEmpty(message = "Cart items cannot be empty")
    private List<CartItemDTO> items;
}
