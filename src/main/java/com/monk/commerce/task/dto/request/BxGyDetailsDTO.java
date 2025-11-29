package com.monk.commerce.task.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BxGyDetailsDTO {

    // Used JSON property names with underscores to match expected input format
    // Added validation annotations to ensure data integrity

    // Lists of products for "buy" and "get" sections must not be empty
    @Valid
    @NotEmpty(message = "Buy products list cannot be empty")
    @JsonProperty("buy_products")
    private List<BxGyProductDTO> buyProducts;
    
    @Valid
    @NotEmpty(message = "Get products list cannot be empty")
    @JsonProperty("get_products")
    private List<BxGyProductDTO> getProducts;
    // Repetition limit must be at least 1
    @NotNull(message = "Repetition limit is required")
    @Min(value = 1, message = "Repetition limit must be at least 1")
    @JsonProperty("repition_limit")
    private Integer repetitionLimit;
}
