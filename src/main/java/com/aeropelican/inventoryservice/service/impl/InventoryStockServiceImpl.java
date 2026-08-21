package com.aeropelican.inventoryservice.service.impl;

import com.aeropelican.inventoryservice.service.InventoryMovementService;
import com.aeropelican.inventoryservice.service.InventoryStockService;
import com.aeropelican.inventoryservice.dto.request.CreateInventoryStockRequestDTO;
import com.aeropelican.inventoryservice.dto.request.StockAdjustmentRequestDTO;
import com.aeropelican.inventoryservice.dto.request.UpdateInventoryStockRequestDTO;
import com.aeropelican.inventoryservice.dto.response.AvailabilityResponseDTO;
import com.aeropelican.inventoryservice.dto.response.InventoryStockResponseDTO;
import com.aeropelican.inventoryservice.entity.InventoryStock;
import com.aeropelican.inventoryservice.enums.InventoryStatus;
import com.aeropelican.inventoryservice.enums.MovementType;
import com.aeropelican.inventoryservice.repository.InventoryStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryStockServiceImpl implements InventoryStockService {

    private final InventoryStockRepository stockRepository;
    private final InventoryMovementService movementService;


    // ============================================================
    // CREATE INVENTORY
    // ============================================================

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {"inventoryById", "availability"},
            allEntries = true
    )
    public InventoryStockResponseDTO create(
            CreateInventoryStockRequestDTO request
    ) {

        log.info(
                "Creating inventory: variantId={}, location={}",
                request.getProductVariantId(),
                request.getLocationCode()
        );

        if (stockRepository.existsByProductVariantIdAndLocationCode(
                request.getProductVariantId(),
                request.getLocationCode()
        )) {

            log.warn(
                    "Inventory already exists: variantId={}, location={}",
                    request.getProductVariantId(),
                    request.getLocationCode()
            );

            throw new RuntimeException(
                    "Inventory already exists for variant and location"
            );
        }

        InventoryStock stock = InventoryStock.builder()
                .productVariantId(request.getProductVariantId())
                .locationCode(request.getLocationCode())
                .quantityOnHand(request.getQuantityOnHand())
                .quantityReserved(0)
                .reorderLevel(request.getReorderLevel())
                .status(
                        calculateStatus(
                                request.getQuantityOnHand(),
                                0,
                                request.getReorderLevel()
                        )
                )
                .build();

        InventoryStock saved = stockRepository.save(stock);

        log.debug(
                "Inventory saved successfully: inventoryId={}",
                saved.getInventoryId()
        );

        /*
         * Create initial stock movement
         */
        if (request.getQuantityOnHand() > 0) {

            movementService.createMovement(
                    saved.getProductVariantId(),
                    saved.getLocationCode(),
                    MovementType.RECEIPT,
                    request.getQuantityOnHand(),
                    "INVENTORY",
                    saved.getInventoryId().toString(),
                    "Initial stock"
            );

            log.debug(
                    "Initial receipt movement created: inventoryId={}, quantity={}",
                    saved.getInventoryId(),
                    request.getQuantityOnHand()
            );
        }

        return toResponse(saved);
    }


    // ============================================================
    // GET INVENTORY BY ID
    // ============================================================

    @Override
    @Cacheable(
            cacheNames = "inventoryById",
            key = "#inventoryId"
    )
    @Transactional(readOnly = true)
    public InventoryStockResponseDTO getById(
            UUID inventoryId
    ) {

        log.debug(
                "Fetching inventory: inventoryId={}",
                inventoryId
        );

        InventoryStock stock = stockRepository.findById(inventoryId)
                .orElseThrow(() -> {

                    log.warn(
                            "Inventory not found: inventoryId={}",
                            inventoryId
                    );

                    return new RuntimeException(
                            "Inventory not found: " + inventoryId
                    );
                });

        return toResponse(stock);
    }


    // ============================================================
    // CHECK AVAILABILITY
    // ============================================================

    @Override
    @Cacheable(
            cacheNames = "availability",
            key = "#productVariantId + ':' + #locationCode"
    )
    @Transactional(readOnly = true)
    public AvailabilityResponseDTO availability(
            Long productVariantId,
            String locationCode
    ) {

        log.debug(
                "Checking inventory availability: variantId={}, location={}",
                productVariantId,
                locationCode
        );

        InventoryStock stock =
                stockRepository
                        .findByProductVariantIdAndLocationCode(
                                productVariantId,
                                locationCode
                        )
                        .orElseThrow(() -> {

                            log.warn(
                                    "Inventory not found: variantId={}, location={}",
                                    productVariantId,
                                    locationCode
                            );

                            return new RuntimeException(
                                    "Inventory not found"
                            );
                        });

        int available =
                stock.getQuantityOnHand()
                        - stock.getQuantityReserved();

        return AvailabilityResponseDTO.builder()
                .productVariantId(
                        stock.getProductVariantId()
                )
                .locationCode(
                        stock.getLocationCode()
                )
                .quantityOnHand(
                        stock.getQuantityOnHand()
                )
                .quantityReserved(
                        stock.getQuantityReserved()
                )
                .availableQuantity(
                        available
                )
                .reorderLevel(
                        stock.getReorderLevel()
                )
                .status(
                        stock.getStatus()
                )
                .available(
                        available > 0
                )
                .build();
    }


    // ============================================================
    // UPDATE INVENTORY
    // ============================================================

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {"inventoryById", "availability"},
            allEntries = true
    )
    public InventoryStockResponseDTO update(
            UUID inventoryId,
            UpdateInventoryStockRequestDTO request
    ) {

        log.info(
                "Updating inventory: inventoryId={}",
                inventoryId
        );

        InventoryStock stock =
                stockRepository.findById(inventoryId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Inventory not found for update: inventoryId={}",
                                    inventoryId
                            );

                            return new RuntimeException(
                                    "Inventory not found"
                            );
                        });

        /*
         * Update location
         */
        if (request.getLocationCode() != null) {

            log.debug(
                    "Updating location: old={}, new={}",
                    stock.getLocationCode(),
                    request.getLocationCode()
            );

            stock.setLocationCode(
                    request.getLocationCode()
            );
        }

        /*
         * Update reorder level
         */
        if (request.getReorderLevel() != null) {

            log.debug(
                    "Updating reorder level: old={}, new={}",
                    stock.getReorderLevel(),
                    request.getReorderLevel()
            );

            stock.setReorderLevel(
                    request.getReorderLevel()
            );
        }

        /*
         * Update status
         */
        if (request.getStatus() != null) {

            stock.setStatus(
                    request.getStatus()
            );

        } else {

            stock.setStatus(
                    calculateStatus(
                            stock.getQuantityOnHand(),
                            stock.getQuantityReserved(),
                            stock.getReorderLevel()
                    )
            );
        }

        InventoryStock updated =
                stockRepository.save(stock);

        log.info(
                "Inventory updated successfully: inventoryId={}",
                inventoryId
        );

        return toResponse(updated);
    }


    // ============================================================
    // ADJUST INVENTORY
    // ============================================================

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {"inventoryById", "availability"},
            allEntries = true
    )
    public InventoryStockResponseDTO adjust(
            UUID inventoryId,
            StockAdjustmentRequestDTO request
    ) {

        log.info(
                "Adjusting inventory: id={}, type={}, quantity={}",
                inventoryId,
                request.getAdjustmentType(),
                request.getQuantity()
        );

        /*
         * findByIdForUpdate() ensures row-level locking
         * during the transaction.
         */
        InventoryStock stock =
                stockRepository.findByIdForUpdate(inventoryId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Inventory not found for adjustment: inventoryId={}",
                                    inventoryId
                            );

                            return new RuntimeException(
                                    "Inventory not found"
                            );
                        });

        String type =
                request.getAdjustmentType()
                        .toUpperCase();

        /*
         * Validate adjustment type
         */
        if (!type.equals("IN") && !type.equals("OUT")) {

            log.warn(
                    "Invalid adjustment type: {}",
                    type
            );

            throw new IllegalArgumentException(
                    "Adjustment type must be IN or OUT"
            );
        }


        // --------------------------------------------------------
        // STOCK IN
        // --------------------------------------------------------

        if (type.equals("IN")) {

            stock.setQuantityOnHand(
                    stock.getQuantityOnHand()
                            + request.getQuantity()
            );

            movementService.createMovement(
                    stock.getProductVariantId(),
                    stock.getLocationCode(),
                    MovementType.ADJUSTMENT_IN,
                    request.getQuantity(),
                    "INVENTORY",
                    inventoryId.toString(),
                    request.getReason()
            );

            log.debug(
                    "Stock increased: inventoryId={}, quantity={}",
                    inventoryId,
                    request.getQuantity()
            );
        }


        // --------------------------------------------------------
        // STOCK OUT
        // --------------------------------------------------------

        else {

            int newQuantity =
                    stock.getQuantityOnHand()
                            - request.getQuantity();

            /*
             * Never allow physical stock to go below
             * already reserved quantity.
             */
            if (newQuantity < stock.getQuantityReserved()) {

                log.warn(
                        "Stock adjustment rejected: inventoryId={}, " +
                                "newQuantity={}, reserved={}",
                        inventoryId,
                        newQuantity,
                        stock.getQuantityReserved()
                );

                throw new IllegalArgumentException(
                        "Cannot reduce stock below reserved quantity"
                );
            }

            stock.setQuantityOnHand(
                    newQuantity
            );

            movementService.createMovement(
                    stock.getProductVariantId(),
                    stock.getLocationCode(),
                    MovementType.ADJUSTMENT_OUT,
                    request.getQuantity(),
                    "INVENTORY",
                    inventoryId.toString(),
                    request.getReason()
            );

            log.debug(
                    "Stock decreased: inventoryId={}, quantity={}",
                    inventoryId,
                    request.getQuantity()
            );
        }


        /*
         * Recalculate inventory status
         */
        stock.setStatus(
                calculateStatus(
                        stock.getQuantityOnHand(),
                        stock.getQuantityReserved(),
                        stock.getReorderLevel()
                )
        );

        InventoryStock updated =
                stockRepository.save(stock);

        log.info(
                "Inventory adjustment completed: inventoryId={}, " +
                        "quantityOnHand={}, status={}",
                inventoryId,
                updated.getQuantityOnHand(),
                updated.getStatus()
        );

        return toResponse(updated);
    }


    // ============================================================
    // CALCULATE INVENTORY STATUS
    // ============================================================

    private InventoryStatus calculateStatus(
            int onHand,
            int reserved,
            int reorderLevel
    ) {

        int available =
                onHand - reserved;

        if (available <= 0) {
            return InventoryStatus.OUT_OF_STOCK;
        }

        if (available <= reorderLevel) {
            return InventoryStatus.LOW_STOCK;
        }

        return InventoryStatus.IN_STOCK;
    }


    // ============================================================
    // ENTITY -> RESPONSE DTO
    // ============================================================

    private InventoryStockResponseDTO toResponse(
            InventoryStock stock
    ) {

        return InventoryStockResponseDTO.builder()
                .inventoryId(
                        stock.getInventoryId()
                )
                .productVariantId(
                        stock.getProductVariantId()
                )
                .locationCode(
                        stock.getLocationCode()
                )
                .quantityOnHand(
                        stock.getQuantityOnHand()
                )
                .quantityReserved(
                        stock.getQuantityReserved()
                )
                .availableQuantity(
                        stock.getQuantityOnHand()
                                - stock.getQuantityReserved()
                )
                .reorderLevel(
                        stock.getReorderLevel()
                )
                .status(
                        stock.getStatus()
                )
                .createdAt(
                        stock.getCreatedAt()
                )
                .updatedAt(
                        stock.getUpdatedAt()
                )
                .build();
    }
}