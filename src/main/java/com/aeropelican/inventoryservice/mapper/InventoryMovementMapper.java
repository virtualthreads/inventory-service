package com.aeropelican.inventoryservice.mapper;

import com.aeropelican.inventoryservice.dto.response.InventoryMovementResponseDTO;
import com.aeropelican.inventoryservice.entity.InventoryMovement;
import org.springframework.stereotype.Component;

@Component
public class InventoryMovementMapper {

    public InventoryMovementResponseDTO toResponse(
        InventoryMovement movement
    ){
        if (movement == null) {
            return null;
        }
        return InventoryMovementResponseDTO.builder()
            .movementId(movement.getMovementId())
            .productVariantId(movement.getProductVariantId())
            .locationCode(movement.getLocationCode())
            .movementType(movement.getMovementType())
            .quantity(movement.getQuantity())
            .referenceType(movement.getReferenceType())
            .referenceId(movement.getReferenceId())
            .createdAt(movement.getCreatedAt())
            .build();
    }
}