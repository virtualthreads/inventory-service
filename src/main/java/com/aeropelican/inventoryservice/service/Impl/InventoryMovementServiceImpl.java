package com.aeropelican.inventoryservice.service.Impl;

import com.aeropelican.inventoryservice.dto.request.InventoryMovementRequest;
import com.aeropelican.inventoryservice.dto.response.InventoryMovementResponse;
import com.aeropelican.inventoryservice.entity.InventoryMovement;
import com.aeropelican.inventoryservice.exception.MovementNotFoundException;
import com.aeropelican.inventoryservice.mapper.InventoryMovementMapper;
import com.aeropelican.inventoryservice.repository.InventoryMovementRepository;
import com.aeropelican.inventoryservice.service.InventoryMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryMovementServiceImpl
        implements InventoryMovementService {

    private final InventoryMovementRepository movementRepository;

    private final InventoryMovementMapper movementMapper;


    // ============================================================
    // CREATE MOVEMENT
    // ============================================================

    @Override
    public InventoryMovementResponse createMovement(
            InventoryMovementRequest request) {

        InventoryMovement movement =
                InventoryMovement.builder()
                        .productVariantId(
                                request.getProductVariantId()
                        )
                        .locationCode(
                                request.getLocationCode()
                        )
                        .movementType(
                                request.getMovementType()
                        )
                        .quantity(
                                request.getQuantity()
                        )
                        .referenceType(
                                request.getReferenceType()
                        )
                        .referenceId(
                                request.getReferenceId()
                        )
                        .notes(
                                request.getNotes()
                        )
                        .build();

        InventoryMovement saved =
                movementRepository.save(movement);

        return movementMapper.toResponse(saved);
    }


    // ============================================================
    // GET MOVEMENT BY ID
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public InventoryMovementResponse getMovementById(
            UUID movementId) {

        InventoryMovement movement =
                movementRepository.findById(movementId)
                        .orElseThrow(
                                () -> new MovementNotFoundException(
                                        movementId
                                )
                        );

        return movementMapper.toResponse(movement);
    }
}