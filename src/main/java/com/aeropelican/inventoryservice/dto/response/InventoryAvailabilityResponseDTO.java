package com.aeropelican.inventoryservice.dto.response;

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
public class InventoryAvailabilityResponseDTO {
    private Long productVariantId;
    private Integer requestedQuantity;
    private Boolean available;
    private Integer availableQuantity;
}