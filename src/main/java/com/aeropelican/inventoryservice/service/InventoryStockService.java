package com.aeropelican.inventoryservice.service;

import com.aeropelican.inventoryservice.dto.request.CreateInventoryStockRequestDTO;
import com.aeropelican.inventoryservice.dto.request.StockAdjustmentRequestDTO;
import com.aeropelican.inventoryservice.dto.request.UpdateInventoryStockRequestDTO;
import com.aeropelican.inventoryservice.dto.response.AvailabilityResponseDTO;
import com.aeropelican.inventoryservice.dto.response.InventoryStockResponseDTO;

import java.util.UUID;

public interface InventoryStockService {

    InventoryStockResponseDTO create(
        CreateInventoryStockRequestDTO request
    );

    InventoryStockResponseDTO getById(
        UUID inventoryId
    );

    AvailabilityResponseDTO availability(
        Long productVariantId,
        String locationCode
    );

    InventoryStockResponseDTO update(
        UUID inventoryId,
        UpdateInventoryStockRequestDTO request
    );

    InventoryStockResponseDTO adjust(
        UUID inventoryId,
        StockAdjustmentRequestDTO request
    );
}