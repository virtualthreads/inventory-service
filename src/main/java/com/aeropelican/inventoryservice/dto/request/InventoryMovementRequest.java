package com.aeropelican.inventoryservice.dto.request;

import com.aeropelican.inventoryservice.enums.MovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovementRequest {

    @NotNull
    private Long productVariantId;


    @NotNull
    private String locationCode;


    @NotNull
    private MovementType movementType;


    @NotNull
    @Min(1)
    private Integer quantity;


    private String referenceType;


    private String referenceId;


    private String notes;
}