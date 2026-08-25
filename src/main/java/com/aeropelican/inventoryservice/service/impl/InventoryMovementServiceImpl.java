package com.aeropelican.inventoryservice.service.impl;

import com.aeropelican.inventoryservice.dto.response.InventoryMovementResponseDTO;
import com.aeropelican.inventoryservice.entity.InventoryMovement;
import com.aeropelican.inventoryservice.enums.MovementType;
import com.aeropelican.inventoryservice.repository.InventoryMovementRepository;
import com.aeropelican.inventoryservice.service.InventoryMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryMovementServiceImpl implements InventoryMovementService {

    private final InventoryMovementRepository inventoryMovementRepository;

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

    @Override
    public Page<InventoryMovementResponseDTO> getByVariant(
            Long variantId,
            Pageable pageable
    ) {

        Page<InventoryMovement> movements =
                inventoryMovementRepository.findByProductVariantId(
                        variantId,
                        pageable
                );

        return movements.map(movement ->
                InventoryMovementResponseDTO.builder()
                        .movementId(movement.getMovementId())
                        .productVariantId(movement.getProductVariantId())
                        .locationCode(movement.getLocationCode())
                        .movementType(movement.getMovementType())
                        .quantity(movement.getQuantity())
                        .referenceType(movement.getReferenceType())
                        .referenceId(movement.getReferenceId())
                        .reason(movement.getReason())
                        .createdAt(movement.getCreatedAt())
                        .build()
        );
    }
}