package com.aeropelican.inventoryservice.service.Impl;
import com.aeropelican.inventoryservice.dto.request.InventoryMovementRequest;
import com.aeropelican.inventoryservice.dto.request.CreateInventoryStockRequestDTO;
import com.aeropelican.inventoryservice.dto.request.StockAdjustmentRequestDTO;
import com.aeropelican.inventoryservice.dto.request.UpdateInventoryStockRequestDTO;
import com.aeropelican.inventoryservice.dto.response.AvailabilityResponseDTO;
import com.aeropelican.inventoryservice.dto.response.InventoryStockResponseDTO;
import com.aeropelican.inventoryservice.entity.InventoryStock;
import com.aeropelican.inventoryservice.enums.MovementType;
import com.aeropelican.inventoryservice.repository.InventoryStockRepository;
import com.aeropelican.inventoryservice.service.InventoryMovementService;
import com.aeropelican.inventoryservice.service.InventoryStockService;
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
public class InventoryStockServiceImpl
        implements InventoryStockService {

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
            CreateInventoryStockRequestDTO request) {

        log.info(
                "Creating inventory: variantId={}, location={}",
                request.getProductVariantId(),
                request.getLocationCode()
        );


        if (stockRepository
                .existsByProductVariantIdAndLocationCode(
                        request.getProductVariantId(),
                        request.getLocationCode()
                )) {

            throw new IllegalArgumentException(
                    "Inventory already exists for product variant "
                            + request.getProductVariantId()
                            + " at location "
                            + request.getLocationCode()
            );
        }


        int quantity =
                request.getQuantityOnHand();


        InventoryStock stock =
                InventoryStock.builder()
                        .productVariantId(
                                request.getProductVariantId()
                        )
                        .locationCode(
                                request.getLocationCode()
                        )
                        .totalQuantity(
                                quantity
                        )
                        .reservedQuantity(
                                0
                        )
                        .availableQuantity(
                                quantity
                        )
                        .build();


        InventoryStock saved =
                stockRepository.save(stock);


        log.info(
                "Inventory created: stockId={}",
                saved.getStockId()
        );


        /*
         * Create initial movement.
         *
         * We are using IN because the current
         * MovementType should represent stock entering
         * inventory.
         */
        if (quantity > 0) {

            movementService.createMovement(
                    InventoryMovementRequest.builder()
                            .productVariantId(
                                    saved.getProductVariantId()
                            )
                            .locationCode(
                                    saved.getLocationCode()
                            )
                            .movementType(
                                    MovementType.IN
                            )
                            .quantity(
                                    quantity
                            )
                            .referenceType(
                                    "INVENTORY"
                            )
                            .referenceId(
                                    saved.getStockId().toString()
                            )
                            .notes(
                                    "Initial stock"
                            )
                            .build()
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
            UUID inventoryId) {

        InventoryStock stock =
                stockRepository.findById(inventoryId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Inventory not found: "
                                                + inventoryId
                                )
                        );


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
            String locationCode) {

        InventoryStock stock =
                stockRepository
                        .findByProductVariantIdAndLocationCode(
                                productVariantId,
                                locationCode
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Inventory not found for "
                                                + "productVariantId="
                                                + productVariantId
                                                + ", locationCode="
                                                + locationCode
                                )
                        );


        int available =
                stock.getAvailableQuantity();


        return AvailabilityResponseDTO.builder()
                .productVariantId(
                        stock.getProductVariantId()
                )
                .locationCode(
                        stock.getLocationCode()
                )
                .totalQuantity(
                        stock.getTotalQuantity()
                )
                .reservedQuantity(
                        stock.getReservedQuantity()
                )
                .availableQuantity(
                        available
                )
                .available(
                        available > 0
                )
                .build();
    }


    // ============================================================
    // UPDATE INVENTORY CONFIGURATION
    // ============================================================

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {"inventoryById", "availability"},
            allEntries = true
    )
    public InventoryStockResponseDTO update(
            UUID inventoryId,
            UpdateInventoryStockRequestDTO request) {

        InventoryStock stock =
                stockRepository.findById(inventoryId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Inventory not found: "
                                                + inventoryId
                                )
                        );


        /*
         * Update location if supplied.
         */
        if (request.getLocationCode() != null) {

            stock.setLocationCode(
                    request.getLocationCode()
            );
        }


        /*
         * We do NOT update quantity here.
         *
         * Quantity changes should happen through
         * inventory movements / adjustments.
         */


        InventoryStock updated =
                stockRepository.save(stock);


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
            StockAdjustmentRequestDTO request) {

        InventoryStock stock =
                stockRepository.findById(inventoryId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Inventory not found: "
                                                + inventoryId
                                )
                        );


        String type =
                request.getAdjustmentType()
                        .toUpperCase();


        if (!type.equals("IN")
                && !type.equals("OUT")) {

            throw new IllegalArgumentException(
                    "Adjustment type must be IN or OUT"
            );
        }


        // ========================================================
        // STOCK IN
        // ========================================================

        if (type.equals("IN")) {

            stock.setTotalQuantity(
                    stock.getTotalQuantity()
                            + request.getQuantity()
            );


            stock.setAvailableQuantity(
                    stock.getAvailableQuantity()
                            + request.getQuantity()
            );


            movementService.createMovement(
                    InventoryMovementRequest.builder()
                            .productVariantId(
                                    stock.getProductVariantId()
                            )
                            .locationCode(
                                    stock.getLocationCode()
                            )
                            .movementType(
                                    MovementType.ADJUSTMENT
                            )
                            .quantity(
                                    request.getQuantity()
                            )
                            .referenceType(
                                    "INVENTORY"
                            )
                            .referenceId(
                                    inventoryId.toString()
                            )
                            .notes(
                                    request.getReason()
                            )
                            .build()
            ); }


        // ========================================================
        // STOCK OUT
        // ========================================================

        else {

            int newAvailable =
                    stock.getAvailableQuantity()
                            - request.getQuantity();


            if (newAvailable < 0) {

                throw new IllegalArgumentException(
                        "Insufficient available inventory"
                );
            }


            stock.setTotalQuantity(
                    stock.getTotalQuantity()
                            - request.getQuantity()
            );


            stock.setAvailableQuantity(
                    newAvailable
            );


            movementService.createMovement(
                    InventoryMovementRequest.builder()
                            .productVariantId(
                                    stock.getProductVariantId()
                            )
                            .locationCode(
                                    stock.getLocationCode()
                            )
                            .movementType(
                                    MovementType.ADJUSTMENT
                            )
                            .quantity(
                                    request.getQuantity()
                            )
                            .referenceType(
                                    "INVENTORY"
                            )
                            .referenceId(
                                    inventoryId.toString()
                            )
                            .notes(
                                    request.getReason()
                            )
                            .build()
            );
        }


        InventoryStock updated =
                stockRepository.save(stock);


        return toResponse(updated);
    }


    // ============================================================
    // ENTITY -> RESPONSE DTO
    // ============================================================

    private InventoryStockResponseDTO toResponse(
            InventoryStock stock) {

        return InventoryStockResponseDTO.builder()
                .stockId(
                        stock.getStockId()
                )
                .productVariantId(
                        stock.getProductVariantId()
                )
                .locationCode(
                        stock.getLocationCode()
                )
                .totalQuantity(
                        stock.getTotalQuantity()
                )
                .reservedQuantity(
                        stock.getReservedQuantity()
                )
                .availableQuantity(
                        stock.getAvailableQuantity()
                )
                .build();
    }
}