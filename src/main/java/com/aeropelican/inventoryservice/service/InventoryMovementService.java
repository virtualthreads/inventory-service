package com.aeropelican.inventoryservice.service;

import com.aeropelican.inventoryservice.enums.MovementType;

public interface InventoryMovementService {

    void createMovement(
            Long productId,
            String variantId,
            MovementType movementType,
            Integer quantity,
            String referenceType,
            String referenceId,
            String notes
    );
}