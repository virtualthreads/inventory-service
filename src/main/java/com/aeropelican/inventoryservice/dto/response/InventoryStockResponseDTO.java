package com.aeropelican.inventoryservice.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryStockResponseDTO {

    private UUID stockId;

    private Long productVariantId;

    private String locationCode;

    private Integer totalQuantity;

    private Integer reservedQuantity;

    private Integer availableQuantity;
}