package com.aeropelican.inventoryservice.service;

import com.aeropelican.inventoryservice.dto.request.AdjustInventoryRequestDTO;
import com.aeropelican.inventoryservice.dto.request.CreateInventoryRequestDTO;
import com.aeropelican.inventoryservice.dto.request.UpdateInventoryRequestDTO;
import com.aeropelican.inventoryservice.dto.response.InventoryAvailabilityResponseDTO;
import com.aeropelican.inventoryservice.dto.response.InventoryResponseDTO;
import com.aeropelican.inventoryservice.dto.response.InventoryStockResponseDTO;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface InventoryService {

    InventoryResponseDTO getInventoryByVariantId(Long variantId);

    InventoryAvailabilityResponseDTO checkAvailability(
            Long variantId,
            Integer quantity
    );

    Page<InventoryStockResponseDTO> searchInventory(
            Long variantId,
            String locationCode,
            String status,
            int page,
            int size
    );

    InventoryStockResponseDTO getInventoryById(UUID inventoryId);

    InventoryStockResponseDTO createInventory(
            CreateInventoryRequestDTO request
    );

    InventoryStockResponseDTO updateInventory(
            UUID inventoryId,
            UpdateInventoryRequestDTO request
    );

    InventoryStockResponseDTO adjustInventory(
            UUID inventoryId,
            AdjustInventoryRequestDTO request
    );
}