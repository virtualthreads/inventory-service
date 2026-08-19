package com.aeropelican.inventoryservice.controller;
import com.aeropelican.inventoryservice.dto.request.CreateInventoryStockRequestDTO;
import com.aeropelican.inventoryservice.dto.request.StockAdjustmentRequestDTO;
import com.aeropelican.inventoryservice.dto.request.UpdateInventoryStockRequestDTO;
import com.aeropelican.inventoryservice.dto.response.APIResponse;
import com.aeropelican.inventoryservice.dto.response.AvailabilityResponseDTO;
import com.aeropelican.inventoryservice.dto.response.InventoryStockResponseDTO;
import com.aeropelican.inventoryservice.service.InventoryStockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryStockController {

    private final InventoryStockService inventoryStockService;
    // GET INVENTORY BY ID
    @GetMapping("/stock/{inventoryId}")
    public ResponseEntity<APIResponse<InventoryStockResponseDTO>> getById(
            @PathVariable UUID inventoryId
    ) {

        InventoryStockResponseDTO response =
                inventoryStockService.getById(inventoryId);

        return ResponseEntity.ok(
                APIResponse.<InventoryStockResponseDTO>builder()
                        .data(response)
                        .message("Inventory fetched successfully")
                        .success(true)
                        .build()
        );
    }


    // ============================================================
    // CHECK AVAILABILITY
    // ============================================================

    @GetMapping("/variants/{variantId}/availability")
    public ResponseEntity<APIResponse<AvailabilityResponseDTO>> availability(
            @PathVariable Long variantId,
            @RequestParam String locationCode
    ) {

        AvailabilityResponseDTO response =
                inventoryStockService.availability(
                        variantId,
                        locationCode
                );

        return ResponseEntity.ok(
                APIResponse.<AvailabilityResponseDTO>builder()
                        .data(response)
                        .message("Inventory availability fetched successfully")
                        .success(true)
                        .build()
        );
    }


    // ============================================================
    // CREATE INVENTORY
    // ============================================================

    @PostMapping("/stock")
    public ResponseEntity<APIResponse<InventoryStockResponseDTO>> create(
            @Valid @RequestBody CreateInventoryStockRequestDTO request
    ) {

        InventoryStockResponseDTO response =
                inventoryStockService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        APIResponse.<InventoryStockResponseDTO>builder()
                                .data(response)
                                .message("Inventory created successfully")
                                .success(true)
                                .build()
                );
    }


    // ============================================================
    // UPDATE INVENTORY CONFIGURATION
    // ============================================================

    @PatchMapping("/stock/{inventoryId}")
    public ResponseEntity<APIResponse<InventoryStockResponseDTO>> update(
            @PathVariable UUID inventoryId,
            @Valid @RequestBody UpdateInventoryStockRequestDTO request
    ) {

        InventoryStockResponseDTO response =
                inventoryStockService.update(
                        inventoryId,
                        request
                );

        return ResponseEntity.ok(
                APIResponse.<InventoryStockResponseDTO>builder()
                        .data(response)
                        .message("Inventory updated successfully")
                        .success(true)
                        .build()
        );
    }


    // ============================================================
    // ADJUST INVENTORY
    // ============================================================

    @PostMapping("/stock/{inventoryId}/adjust")
    public ResponseEntity<APIResponse<InventoryStockResponseDTO>> adjust(
            @PathVariable UUID inventoryId,
            @Valid @RequestBody StockAdjustmentRequestDTO request
    ) {

        InventoryStockResponseDTO response =
                inventoryStockService.adjust(
                        inventoryId,
                        request
                );

        return ResponseEntity.ok(
                APIResponse.<InventoryStockResponseDTO>builder()
                        .data(response)
                        .message("Inventory adjusted successfully")
                        .success(true)
                        .build()
        );
    }
}