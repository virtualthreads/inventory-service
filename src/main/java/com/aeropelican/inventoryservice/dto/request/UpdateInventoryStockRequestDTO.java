package com.aeropelican.inventoryservice.dto.request;
import com.aeropelican.inventoryservice.enums.InventoryStatus;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateInventoryStockRequestDTO {

    @Size(max = 30, message = "Location code must not exceed 30 characters")
    private String locationCode;

    @PositiveOrZero(message = "Reorder level cannot be negative")
    private Integer reorderLevel;

    private InventoryStatus status;
}