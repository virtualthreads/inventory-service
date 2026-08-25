package com.aeropelican.inventoryservice.service.impl;

import com.aeropelican.inventoryservice.dto.request.AdjustInventoryRequestDTO;
import com.aeropelican.inventoryservice.dto.request.CreateInventoryRequestDTO;
import com.aeropelican.inventoryservice.dto.request.UpdateInventoryRequestDTO;
import com.aeropelican.inventoryservice.dto.response.InventoryAvailabilityResponseDTO;
import com.aeropelican.inventoryservice.dto.response.InventoryResponseDTO;
import com.aeropelican.inventoryservice.dto.response.InventoryStockResponseDTO;
import com.aeropelican.inventoryservice.entity.InventoryMovement;
import com.aeropelican.inventoryservice.entity.InventoryStock;
import com.aeropelican.inventoryservice.exception.DuplicateInventoryException;
import com.aeropelican.inventoryservice.exception.InsufficientInventoryException;
import com.aeropelican.inventoryservice.exception.InventoryNotFoundException;
import com.aeropelican.inventoryservice.mapper.InventoryMapper;
import com.aeropelican.inventoryservice.repository.InventoryMovementRepository;
import com.aeropelican.inventoryservice.repository.InventoryStockRepository;
import com.aeropelican.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryStockRepository inventoryStockRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    // ============================================================
    // GET INVENTORY BY VARIANT
    // ============================================================

    @Override
    public InventoryResponseDTO getInventoryByVariantId(Long variantId) {

        log.info(
                "Fetching inventory for product variant: {}",
                variantId
        );

        List<InventoryStock> inventoryStocks =
                inventoryStockRepository.findByProductVariantId(
                        variantId
                );

        if (inventoryStocks.isEmpty()) {

            throw new InventoryNotFoundException(
                    "No inventory found for product variant: "
                            + variantId
            );
        }

        int totalQuantityOnHand =
                inventoryStocks.stream()
                        .mapToInt(stock ->
                                stock.getQuantityOnHand() == null
                                        ? 0
                                        : stock.getQuantityOnHand()
                        )
                        .sum();

        int totalQuantityReserved =
                inventoryStocks.stream()
                        .mapToInt(stock ->
                                stock.getQuantityReserved() == null
                                        ? 0
                                        : stock.getQuantityReserved()
                        )
                        .sum();

        int totalAvailableQuantity =
                totalQuantityOnHand - totalQuantityReserved;

        return InventoryResponseDTO.builder()
                .productVariantId(variantId)
                .totalQuantityOnHand(totalQuantityOnHand)
                .totalQuantityReserved(totalQuantityReserved)
                .totalAvailableQuantity(totalAvailableQuantity)
                .locations(
                        inventoryStocks.stream()
                                .map(InventoryMapper::toLocationResponse)
                                .toList()
                )
                .build();
    }

    // ============================================================
    // CHECK AVAILABILITY
    // ============================================================

    @Override
    public InventoryAvailabilityResponseDTO checkAvailability(
            Long variantId,
            Integer quantity) {

        log.info(
                "Checking availability. variantId={}, quantity={}",
                variantId,
                quantity
        );

        if (quantity == null || quantity < 1) {

            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        List<InventoryStock> inventoryStocks =
                inventoryStockRepository.findByProductVariantId(
                        variantId
                );

        if (inventoryStocks.isEmpty()) {

            throw new InventoryNotFoundException(
                    "No inventory found for product variant: "
                            + variantId
            );
        }

        int availableQuantity =
                inventoryStocks.stream()
                        .mapToInt(stock -> {

                            int onHand =
                                    stock.getQuantityOnHand() == null
                                            ? 0
                                            : stock.getQuantityOnHand();

                            int reserved =
                                    stock.getQuantityReserved() == null
                                            ? 0
                                            : stock.getQuantityReserved();

                            return onHand - reserved;
                        })
                        .sum();

        boolean available =
                availableQuantity >= quantity;

        return InventoryAvailabilityResponseDTO.builder()
                .productVariantId(variantId)
                .requestedQuantity(quantity)
                .available(available)
                .availableQuantity(availableQuantity)
                .build();
    }

    // ============================================================
    // SEARCH INVENTORY
    // ============================================================

    @Override
    public Page<InventoryStockResponseDTO> searchInventory(
            Long variantId,
            String locationCode,
            String status,
            int page,
            int size) {

        log.info(
                "Searching inventory. variantId={}, locationCode={}, status={}, page={}, size={}",
                variantId,
                locationCode,
                status,
                page,
                size
        );

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must be greater than or equal to zero"
            );
        }

        if (size < 1) {
            throw new IllegalArgumentException(
                    "Size must be greater than zero"
            );
        }

        if (locationCode != null && locationCode.isBlank()) {
            locationCode = null;
        }

        if (status != null && status.isBlank()) {
            status = null;
        }

        Pageable pageable =
                PageRequest.of(page, size);

        return inventoryStockRepository
                .searchInventory(
                        variantId,
                        locationCode,
                        status,
                        pageable
                )
                .map(InventoryMapper::toStockResponse);
    }

    // ============================================================
    // GET INVENTORY BY ID
    // ============================================================

    @Override
    public InventoryStockResponseDTO getInventoryById(
            UUID inventoryId) {

        log.info(
                "Fetching inventory by ID: {}",
                inventoryId
        );

        InventoryStock inventoryStock =
                inventoryStockRepository.findById(inventoryId)
                        .orElseThrow(() ->
                                new InventoryNotFoundException(
                                        "Inventory not found with id: "
                                                + inventoryId
                                )
                        );

        return InventoryMapper.toStockResponse(
                inventoryStock
        );
    }

    // ============================================================
    // CREATE INITIAL INVENTORY
    // ============================================================

    @Override
    @Transactional
    public InventoryStockResponseDTO createInventory(
            CreateInventoryRequestDTO request) {

        log.info(
                "Creating inventory. variantId={}, locationCode={}",
                request.getProductVariantId(),
                request.getLocationCode()
        );

        InventoryStock existing =
                inventoryStockRepository
                        .findByProductVariantIdAndLocationCode(
                                request.getProductVariantId(),
                                request.getLocationCode()
                        )
                        .orElse(null);

        if (existing != null) {

            throw new DuplicateInventoryException(
                    "Inventory already exists for product variant "
                            + request.getProductVariantId()
                            + " at location "
                            + request.getLocationCode()
            );
        }

        int quantityOnHand =
                request.getQuantityOnHand();

        int reorderLevel =
                request.getReorderLevel() == null
                        ? 10
                        : request.getReorderLevel();

        InventoryStock inventoryStock =
                InventoryStock.builder()
                        .inventoryId(UUID.randomUUID())
                        .productVariantId(
                                request.getProductVariantId()
                        )
                        .locationCode(
                                request.getLocationCode()
                        )
                        .quantityOnHand(
                                quantityOnHand
                        )
                        .quantityReserved(0)
                        .reorderLevel(
                                reorderLevel
                        )
                        .status(
                                determineStatus(
                                        quantityOnHand,
                                        reorderLevel
                                )
                        )
                        .createdAt(
                                LocalDateTime.now()
                        )
                        .updatedAt(
                                LocalDateTime.now()
                        )
                        .build();

        InventoryStock saved =
                inventoryStockRepository.save(
                        inventoryStock
                );

        log.info(
                "Inventory created successfully. inventoryId={}",
                saved.getInventoryId()
        );

        return InventoryMapper.toStockResponse(
                saved
        );
    }

    // ============================================================
    // UPDATE INVENTORY CONFIGURATION
    // ============================================================

    @Override
    @Transactional
    public InventoryStockResponseDTO updateInventory(
            UUID inventoryId,
            UpdateInventoryRequestDTO request) {

        log.info(
                "Updating inventory configuration. inventoryId={}",
                inventoryId
        );

        InventoryStock inventoryStock =
                inventoryStockRepository.findById(inventoryId)
                        .orElseThrow(() ->
                                new InventoryNotFoundException(
                                        "Inventory not found with id: "
                                                + inventoryId
                                )
                        );

        if (request.getReorderLevel() != null) {

            inventoryStock.setReorderLevel(
                    request.getReorderLevel()
            );

            /*
             * Recalculate automatic stock status when
             * reorder level changes.
             *
             * If caller explicitly supplies status,
             * that status will be applied below.
             */
            inventoryStock.setStatus(
                    determineStatus(
                            inventoryStock.getQuantityOnHand(),
                            request.getReorderLevel()
                    )
            );
        }

        if (request.getStatus() != null
                && !request.getStatus().isBlank()) {

            validateStatus(request.getStatus());

            inventoryStock.setStatus(
                    request.getStatus().toUpperCase()
            );
        }

        inventoryStock.setUpdatedAt(
                LocalDateTime.now()
        );

        InventoryStock saved =
                inventoryStockRepository.save(
                        inventoryStock
                );

        return InventoryMapper.toStockResponse(
                saved
        );
    }

    // ============================================================
    // ADJUST PHYSICAL STOCK
    // ============================================================

    @Override
    @Transactional
    public InventoryStockResponseDTO adjustInventory(
            UUID inventoryId,
            AdjustInventoryRequestDTO request) {

        log.info(
                "Adjusting inventory. inventoryId={}, movementType={}, quantity={}",
                inventoryId,
                request.getMovementType(),
                request.getQuantity()
        );

        InventoryStock inventoryStock =
                inventoryStockRepository.findById(
                                inventoryId
                        )
                        .orElseThrow(() ->
                                new InventoryNotFoundException(
                                        "Inventory not found with id: "
                                                + inventoryId
                                )
                        );

        if (request.getQuantity() == null
                || request.getQuantity() <= 0) {

            throw new IllegalArgumentException(
                    "Adjustment quantity must be greater than zero"
            );
        }

        if (request.getMovementType() == null
                || request.getMovementType().isBlank()) {

            throw new IllegalArgumentException(
                    "Movement type is required"
            );
        }

        String movementType =
                request.getMovementType()
                        .trim()
                        .toUpperCase();

        if (!movementType.equals("ADJUSTMENT_IN")
                && !movementType.equals("ADJUSTMENT_OUT")) {

            throw new IllegalArgumentException(
                    "Movement type must be ADJUSTMENT_IN "
                            + "or ADJUSTMENT_OUT"
            );
        }

        int currentQuantity =
                inventoryStock.getQuantityOnHand();

        int currentReserved =
                inventoryStock.getQuantityReserved();

        int adjustmentQuantity =
                request.getQuantity();

        // --------------------------------------------------------
        // ADD STOCK
        // --------------------------------------------------------

        if (movementType.equals("ADJUSTMENT_IN")) {

            inventoryStock.setQuantityOnHand(
                    currentQuantity + adjustmentQuantity
            );
        }

        // --------------------------------------------------------
        // REMOVE STOCK
        // --------------------------------------------------------

        else {

            int availableQuantity =
                    currentQuantity - currentReserved;

            if (adjustmentQuantity > availableQuantity) {

                throw new InsufficientInventoryException(
                        "Insufficient available stock for adjustment. "
                                + "Available: "
                                + availableQuantity
                                + ", requested: "
                                + adjustmentQuantity
                );
            }

            inventoryStock.setQuantityOnHand(
                    currentQuantity - adjustmentQuantity
            );
        }

        /*
         * IMPORTANT:
         *
         * quantityReserved is NEVER changed here.
         *
         * This API adjusts physical stock only.
         */
        inventoryStock.setStatus(
                determineStatus(
                        inventoryStock.getQuantityOnHand(),
                        inventoryStock.getReorderLevel()
                )
        );

        inventoryStock.setUpdatedAt(
                LocalDateTime.now()
        );

        InventoryStock saved =
                inventoryStockRepository.save(
                        inventoryStock
                );

        // --------------------------------------------------------
        // INSERT APPEND-ONLY MOVEMENT
        // --------------------------------------------------------

        InventoryMovement.MovementType movementEnum =
                InventoryMovement.MovementType.valueOf(
                        movementType
                );

        InventoryMovement movement =
                InventoryMovement.builder()
                        .movementId(UUID.randomUUID())
                        .productVariantId(
                                saved.getProductVariantId()
                        )
                        .locationCode(
                                saved.getLocationCode()
                        )
                        .movementType(
                                movementEnum
                        )
                        .quantity(
                                adjustmentQuantity
                        )
                        .referenceType(
                                "INVENTORY"
                        )
                        .referenceId(
                                saved.getInventoryId().toString()
                        )
                        .reason(
                                request.getReason()
                        )
                        .createdAt(
                                LocalDateTime.now()
                        )
                        .build();

        inventoryMovementRepository.save(
                movement
        );

        log.info(
                "Inventory adjustment completed. inventoryId={}, movementId={}, quantityOnHand={}",
                saved.getInventoryId(),
                movement.getMovementId(),
                saved.getQuantityOnHand()
        );

        return InventoryMapper.toStockResponse(
                saved
        );
    }

    // ============================================================
    // DETERMINE STATUS
    // ============================================================

    private String determineStatus(
            Integer quantityOnHand,
            Integer reorderLevel) {

        if (quantityOnHand == null
                || quantityOnHand <= 0) {

            return "OUT_OF_STOCK";
        }

        int level =
                reorderLevel == null
                        ? 10
                        : reorderLevel;

        if (quantityOnHand <= level) {
            return "LOW_STOCK";
        }

        return "IN_STOCK";
    }

    // ============================================================
    // VALIDATE STATUS
    // ============================================================

    private void validateStatus(String status) {

        String normalizedStatus =
                status.trim().toUpperCase();

        if (!normalizedStatus.equals("IN_STOCK")
                && !normalizedStatus.equals("LOW_STOCK")
                && !normalizedStatus.equals("OUT_OF_STOCK")
                && !normalizedStatus.equals("INACTIVE")) {

            throw new IllegalArgumentException(
                    "Invalid inventory status. Allowed values: "
                            + "IN_STOCK, LOW_STOCK, OUT_OF_STOCK, INACTIVE"
            );
        }
    }
}