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
public class CreateInventoryRequestDTO {

    @NotNull(message = "Product variant ID is required")
    @Min(value = 1, message = "Product variant ID must be greater than 0")
    private Long productVariantId;

    @NotBlank(message = "Location code is required")
    private String locationCode;

    @NotNull(message = "Quantity on hand is required")
    @Min(value = 0, message = "Quantity on hand cannot be negative")
    private Integer quantityOnHand;

    @Min(value = 0, message = "Reorder level cannot be negative")
    private Integer reorderLevel;
}