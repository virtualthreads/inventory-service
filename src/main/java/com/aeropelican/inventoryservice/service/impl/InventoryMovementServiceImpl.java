package com.aeropelican.inventoryservice.service.impl;
import com.aeropelican.inventoryservice.dto.response.InventoryMovementResponseDTO;
import com.aeropelican.inventoryservice.entity.InventoryMovement;
import com.aeropelican.inventoryservice.enums.MovementType;
import com.aeropelican.inventoryservice.repository.InventoryMovementRepository;
import com.aeropelican.inventoryservice.service.InventoryMovementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryMovementServiceImpl implements InventoryMovementService {
    private final InventoryMovementRepository movementRepository;

    // CREATE INVENTORY MOVEMENT

    @Override
    @Transactional
    public void createMovement(
        Long productVariantId,
        String locationCode,
        MovementType movementType,
        Integer quantity,
        String referenceType,
        String referenceId,
        String reason
    ){
        log.info(
            "Creating inventory movement: variantId={}, location={}, type={}, quantity={}",
            productVariantId,
            locationCode,
            movementType,
            quantity
        );
        InventoryMovement movement = InventoryMovement.builder()
            .productVariantId(productVariantId)
            .locationCode(locationCode)
            .movementType(movementType)
            .quantity(quantity)
            .referenceType(referenceType)
            .referenceId(referenceId)
            .reason(reason)
            .build();

        InventoryMovement saved = movementRepository.save(movement);
        log.debug("Inventory movement created successfully: movementId={}",
            saved.getMovementId()
        );
    }

    // GET MOVEMENTS BY PRODUCT VARIANT

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryMovementResponseDTO> getByVariant(
        Long productVariantId,
        Pageable pageable
    ){
        log.debug("Fetching inventory movements: variantId={}, page={}, size={}",
            productVariantId,
            pageable.getPageNumber(),
            pageable.getPageSize()
        );
        Page<InventoryMovementResponseDTO> movements = movementRepository
            .findByProductVariantId(
                productVariantId,
                pageable
            )
            .map(this::toResponse);
        log.debug("Fetched {} inventory movements for variantId={}",
            movements.getNumberOfElements(),
            productVariantId
        );
        return movements;
    }

    // ENTITY -> RESPONSE DTO

    private InventoryMovementResponseDTO toResponse(
        InventoryMovement movement
    ){
        return InventoryMovementResponseDTO.builder()
            .movementId(
                movement.getMovementId()
            )
            .productVariantId(
                movement.getProductVariantId()
            )
            .locationCode(
                movement.getLocationCode()
            )
            .movementType(
                movement.getMovementType()
            )
            .quantity(
                movement.getQuantity()
            )
            .referenceType(
                movement.getReferenceType()
            )
            .referenceId(
                movement.getReferenceId()
            )
            .reason(
                movement.getReason()
            )
            .createdAt(
                 movement.getCreatedAt()
            )
            .build();
    }
}