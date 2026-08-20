package com.aeropelican.inventoryservice.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventoryLocationResponseDTO {

    private String locationCode;

    private Integer quantityOnHand;

    private Integer quantityReserved;

    private Integer availableQuantity;

    private Integer reorderLevel;

    private String status;
}