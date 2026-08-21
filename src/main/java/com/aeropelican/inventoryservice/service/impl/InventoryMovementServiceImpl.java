package com.aeropelican.inventoryservice.service.impl;

import com.aeropelican.inventoryservice.enums.MovementType;
import com.aeropelican.inventoryservice.service.InventoryMovementService;
import org.springframework.stereotype.Service;

@Service
public class InventoryMovementServiceImpl implements InventoryMovementService {

    @Override
    public void createMovement(
            Long productId,
            String variantId,
            MovementType movementType,
            Integer quantity,
            String referenceType,
            String referenceId,
            String notes
    ) {
        // TODO: Implement inventory movement creation
    }
}