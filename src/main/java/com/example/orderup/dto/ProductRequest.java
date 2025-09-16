package com.example.orderup.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    @Min(value = 0, message = "Stock cannot be negative")
    private int stock;
}
