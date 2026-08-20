package com.aeropelican.inventoryservice.service;
import com.aeropelican.inventoryservice.dto.response.InventoryMovementResponseDTO;
import com.aeropelican.inventoryservice.enums.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryMovementService {

    void createMovement(
            Long productVariantId,
            String locationCode,
            MovementType movementType,
            Integer quantity,
            String referenceType,
            String referenceId,
            String reason
    );

    Page<InventoryMovementResponseDTO> getByVariant(
            Long productVariantId,
            Pageable pageable
    );
}
