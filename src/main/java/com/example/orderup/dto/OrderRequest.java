package com.example.orderup.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {

    @NotNull
    private Long productId;

    @NotEmpty
    private String customerName;

    @Min(1)
    private int quantity;
}
