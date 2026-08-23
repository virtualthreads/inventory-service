package com.aeropelican.inventoryservice.mapper;

import com.aeropelican.inventoryservice.dto.response.InventoryMovementResponse;
import com.aeropelican.inventoryservice.entity.InventoryMovement;
import org.springframework.stereotype.Component;

@Component
public class InventoryMovementMapper {

    public InventoryMovementResponse toResponse(
            InventoryMovement entity) {

        if (entity == null) {
            return null;
        }

        return InventoryMovementResponse.builder()
                .movementId(
                        entity.getMovementId()
                )
                .productVariantId(
                        entity.getProductVariantId()
                )
                .locationCode(
                        entity.getLocationCode()
                )
                .movementType(
                        entity.getMovementType()
                )
                .quantity(
                        entity.getQuantity()
                )
                .referenceType(
                        entity.getReferenceType()
                )
                .referenceId(
                        entity.getReferenceId()
                )
                .notes(
                        entity.getNotes()
                )
                .createdAt(
                        entity.getCreatedAt()
                )
                .build();
    }
}