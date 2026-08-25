package com.aeropelican.inventoryservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStockResponseDTO {

    private UUID inventoryId;

    private Long productVariantId;

    private String locationCode;

    private Integer quantityOnHand;

    private Integer quantityReserved;

    private Integer availableQuantity;

    private Integer reorderLevel;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}