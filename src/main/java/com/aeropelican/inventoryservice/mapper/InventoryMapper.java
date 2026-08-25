package com.aeropelican.inventoryservice.mapper;

import com.aeropelican.inventoryservice.dto.response.InventoryLocationResponseDTO;
import com.aeropelican.inventoryservice.dto.response.InventoryStockResponseDTO;
import com.aeropelican.inventoryservice.entity.InventoryStock;

public final class InventoryMapper {

    private InventoryMapper() {
    }

    public static InventoryLocationResponseDTO toLocationResponse(
            InventoryStock inventoryStock) {

        int quantityOnHand = inventoryStock.getQuantityOnHand() == null
                ? 0
                : inventoryStock.getQuantityOnHand();

        int quantityReserved = inventoryStock.getQuantityReserved() == null
                ? 0
                : inventoryStock.getQuantityReserved();

        return InventoryLocationResponseDTO.builder()
                .locationCode(inventoryStock.getLocationCode())
                .quantityOnHand(quantityOnHand)
                .quantityReserved(quantityReserved)
                .availableQuantity(
                        quantityOnHand - quantityReserved
                )
                .reorderLevel(inventoryStock.getReorderLevel())
                .status(inventoryStock.getStatus())
                .build();
    }

    public static InventoryStockResponseDTO toStockResponse(
            InventoryStock inventoryStock) {

        int quantityOnHand = inventoryStock.getQuantityOnHand() == null
                ? 0
                : inventoryStock.getQuantityOnHand();

        int quantityReserved = inventoryStock.getQuantityReserved() == null
                ? 0
                : inventoryStock.getQuantityReserved();

        return InventoryStockResponseDTO.builder()
                .inventoryId(inventoryStock.getInventoryId())
                .productVariantId(inventoryStock.getProductVariantId())
                .locationCode(inventoryStock.getLocationCode())
                .quantityOnHand(quantityOnHand)
                .quantityReserved(quantityReserved)
                .availableQuantity(
                        quantityOnHand - quantityReserved
                )
                .reorderLevel(inventoryStock.getReorderLevel())
                .status(inventoryStock.getStatus())
                .createdAt(inventoryStock.getCreatedAt())
                .updatedAt(inventoryStock.getUpdatedAt())
                .build();
    }
}