package com.aeropelican.inventoryservice.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInventoryStockRequestDTO {

    @NotNull(message = "Product variant ID is required")
    private Long productVariantId;

    @NotBlank(message = "Location code is required")
    @Size(max = 30, message = "Location code must not exceed 30 characters")
    private String locationCode;

    @NotNull(message = "Quantity on hand is required")
    @PositiveOrZero(message = "Quantity on hand cannot be negative")
    private Integer quantityOnHand;

    @NotNull(message = "Reorder level is required")
    @PositiveOrZero(message = "Reorder level cannot be negative")
    private Integer reorderLevel;
}

