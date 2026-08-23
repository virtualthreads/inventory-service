package com.aeropelican.inventoryservice.service;

import com.aeropelican.inventoryservice.dto.request.InventoryMovementRequest;
import com.aeropelican.inventoryservice.dto.response.InventoryMovementResponse;

import java.util.UUID;

public interface InventoryMovementService {

    InventoryMovementResponse createMovement(
            InventoryMovementRequest request
    );

    InventoryMovementResponse getMovementById(
            UUID movementId
    );
}