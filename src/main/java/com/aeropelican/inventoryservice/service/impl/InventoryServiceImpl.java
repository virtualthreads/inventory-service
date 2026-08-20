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
public class InventoryServiceImpl implements InventoryService {

    private final InventoryStockRepository inventoryStockRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    @Override
    public InventoryResponseDTO getInventoryByVariantId(Long variantId) {

        List<InventoryStock> inventoryStocks =
                inventoryStockRepository.findByProductVariantId(variantId);

        int totalQuantityOnHand = inventoryStocks.stream()
                .mapToInt(InventoryStock::getQuantityOnHand)
                .sum();

        int totalQuantityReserved = inventoryStocks.stream()
                .mapToInt(InventoryStock::getQuantityReserved)
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

    @Override
    public InventoryAvailabilityResponseDTO checkAvailability(
            Long variantId,
            Integer quantity) {

        List<InventoryStock> inventoryStocks =
                inventoryStockRepository.findByProductVariantId(variantId);

        int availableQuantity = inventoryStocks.stream()
                .mapToInt(stock ->
                        stock.getQuantityOnHand()
                                - stock.getQuantityReserved())
                .sum();

        return InventoryAvailabilityResponseDTO.builder()
                .productVariantId(variantId)
                .requestedQuantity(quantity)
                .available(quantity <= availableQuantity)
                .availableQuantity(availableQuantity)
                .build();
    }

    @Override
    public Page<InventoryStockResponseDTO> searchInventory(
            Long variantId,
            String locationCode,
            String status,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        return inventoryStockRepository
                .searchInventory(
                        variantId,
                        locationCode,
                        status,
                        pageable
                )
                .map(InventoryMapper::toStockResponse);
    }

    @Override
    public InventoryStockResponseDTO getInventoryById(
            UUID inventoryId) {

        InventoryStock inventoryStock =
                inventoryStockRepository.findById(inventoryId)
                        .orElseThrow(() ->
                                new InventoryNotFoundException(
                                        "Inventory not found with id: "
                                                + inventoryId
                                )
                        );

        return InventoryMapper.toStockResponse(inventoryStock);
    }

    @Override
    @Transactional
    public InventoryStockResponseDTO createInventory(
            CreateInventoryRequestDTO request) {

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

        InventoryStock inventoryStock = InventoryStock.builder()
                .inventoryId(UUID.randomUUID())
                .productVariantId(request.getProductVariantId())
                .locationCode(request.getLocationCode())
                .quantityOnHand(request.getQuantityOnHand())
                .quantityReserved(0)
                .reorderLevel(
                        request.getReorderLevel() == null
                                ? 10
                                : request.getReorderLevel()
                )
                .status(
                        determineStatus(
                                request.getQuantityOnHand(),
                                request.getReorderLevel()
                        )
                )
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        InventoryStock saved =
                inventoryStockRepository.save(inventoryStock);

        return InventoryMapper.toStockResponse(saved);
    }

    @Override
    @Transactional
    public InventoryStockResponseDTO updateInventory(
            UUID inventoryId,
            UpdateInventoryRequestDTO request) {

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
        }

        if (request.getStatus() != null
                && !request.getStatus().isBlank()) {

            inventoryStock.setStatus(
                    request.getStatus()
            );
        }

        inventoryStock.setUpdatedAt(LocalDateTime.now());

        InventoryStock saved =
                inventoryStockRepository.save(inventoryStock);

        return InventoryMapper.toStockResponse(saved);
    }

    @Override
    @Transactional
    public InventoryStockResponseDTO adjustInventory(
            UUID inventoryId,
            AdjustInventoryRequestDTO request) {

        InventoryStock inventoryStock =
                inventoryStockRepository
                        .findById(inventoryId)
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

        if (!"ADJUSTMENT_IN".equals(request.getMovementType())
                && !"ADJUSTMENT_OUT".equals(
                request.getMovementType())) {

            throw new IllegalArgumentException(
                    "Movement type must be ADJUSTMENT_IN "
                            + "or ADJUSTMENT_OUT"
            );
        }

        if ("ADJUSTMENT_IN".equals(request.getMovementType())) {

            inventoryStock.setQuantityOnHand(
                    inventoryStock.getQuantityOnHand()
                            + request.getQuantity()
            );

        } else {

            int availableQuantity =
                    inventoryStock.getQuantityOnHand()
                            - inventoryStock.getQuantityReserved();

            if (request.getQuantity() > availableQuantity) {

                throw new InsufficientInventoryException(
                        "Insufficient available stock for adjustment"
                );
            }

            inventoryStock.setQuantityOnHand(
                    inventoryStock.getQuantityOnHand()
                            - request.getQuantity()
            );
        }

        inventoryStock.setStatus(
                determineStatus(
                        inventoryStock.getQuantityOnHand(),
                        inventoryStock.getReorderLevel()
                )
        );

        inventoryStock.setUpdatedAt(LocalDateTime.now());

        InventoryStock saved =
                inventoryStockRepository.save(inventoryStock);

        InventoryMovement movement =
                InventoryMovement.builder()
                        .movementId(UUID.randomUUID())
                        .productVariantId(
                                inventoryStock.getProductVariantId()
                        )
                        .locationCode(
                                inventoryStock.getLocationCode()
                        )
                        .movementType(
                                request.getMovementType()
                        )
                        .quantity(request.getQuantity())
                        .referenceType("INVENTORY")
                        .referenceId(
                                inventoryStock.getInventoryId().toString()
                        )
                        .reason(request.getReason())
                        .createdAt(LocalDateTime.now())
                        .build();

        inventoryMovementRepository.save(movement);

        return InventoryMapper.toStockResponse(saved);
    }

    private String determineStatus(
            Integer quantityOnHand,
            Integer reorderLevel) {

        if (quantityOnHand == null || quantityOnHand <= 0) {
            return "OUT_OF_STOCK";
        }

        int level = reorderLevel == null
                ? 10
                : reorderLevel;

        if (quantityOnHand <= level) {
            return "LOW_STOCK";
        }

        return "IN_STOCK";
    }
}