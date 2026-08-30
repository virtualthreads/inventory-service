package com.aeropelican.inventoryservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustInventoryRequestDTO {

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Adjustment quantity must be greater than zero")
    private Integer quantity;

    @NotBlank(message = "Movement type is required")
    private String movementType;
    private String reason;
}