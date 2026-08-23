package com.aeropelican.inventoryservice.mapper;

import com.aeropelican.inventoryservice.dto.response.InventoryStockResponseDTO;
import com.aeropelican.inventoryservice.entity.InventoryStock;
import org.springframework.stereotype.Component;

@Component
public class InventoryStockMapper {

    public InventoryStockResponseDTO toResponse(
            InventoryStock stock
    ) {

        if (stock == null) {
            return null;
        }

        return InventoryStockResponseDTO.builder()
                .stockId(stock.getStockId())
                .productVariantId(stock.getProductVariantId())
                .locationCode(stock.getLocationCode())
                .totalQuantity(stock.getTotalQuantity())
                .reservedQuantity(stock.getReservedQuantity())
                .availableQuantity(stock.getAvailableQuantity())
                .build();
    }
}