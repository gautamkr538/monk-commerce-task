package com.monk.commerce.task.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @Valid
    @NotEmpty(message = "Cart items cannot be empty")
    private List<CartItemDTO> items;

    @JsonProperty("user_id")
    private String userId;
}
