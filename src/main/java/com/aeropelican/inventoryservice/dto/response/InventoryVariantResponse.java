package com.aeropelican.inventoryservice.dto.response;

import com.aeropelican.inventoryservice.model.InventoryStock;

import java.util.List;

public record InventoryVariantResponse(
        Long productVariantId,
        int totalQuantityOnHand,
        int totalQuantityReserved,
        int totalAvailableQuantity,
        List<InventoryStock> locations
) {
}