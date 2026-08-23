package com.aeropelican.inventoryservice.dto.response;

import com.aeropelican.inventoryservice.enums.InventoryStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityResponseDTO {

    private Long productVariantId;

    private String locationCode;

    private Integer quantityOnHand;

    private Integer quantityReserved;

    private Integer availableQuantity;

    private Integer reorderLevel;

    private InventoryStatus status;

    private Boolean available;
}