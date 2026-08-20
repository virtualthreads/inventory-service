package com.aeropelican.inventoryservice.controller;

import com.aeropelican.inventoryservice.dto.request.AdjustInventoryRequestDTO;
import com.aeropelican.inventoryservice.dto.request.CreateInventoryRequestDTO;
import com.aeropelican.inventoryservice.dto.request.UpdateInventoryRequestDTO;
import com.aeropelican.inventoryservice.dto.response.InventoryAvailabilityResponseDTO;
import com.aeropelican.inventoryservice.dto.response.InventoryResponseDTO;
import com.aeropelican.inventoryservice.dto.response.InventoryStockResponseDTO;
import com.aeropelican.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // ============================================================
    // 1. GET INVENTORY BY PRODUCT VARIANT
    // GET /api/v1/inventory/variants/{variantId}
    // ============================================================

    @GetMapping("/variants/{variantId}")
    public ResponseEntity<InventoryResponseDTO> getInventoryByVariantId(
            @PathVariable Long variantId) {

        return ResponseEntity.ok(
                inventoryService.getInventoryByVariantId(variantId)
        );
    }

    // ============================================================
    // 2. CHECK INVENTORY AVAILABILITY
    // GET /api/v1/inventory/variants/{variantId}/availability
    // ============================================================

    @GetMapping("/variants/{variantId}/availability")
    public ResponseEntity<InventoryAvailabilityResponseDTO> checkAvailability(
            @PathVariable Long variantId,
            @RequestParam(defaultValue = "1") int quantity) {

        return ResponseEntity.ok(
                inventoryService.checkAvailability(
                        variantId,
                        quantity
                )
        );
    }

    // ============================================================
    // 3. SEARCH INVENTORY
    // GET /api/v1/inventory/stock
    // ============================================================

    @GetMapping("/stock")
    public ResponseEntity<Page<InventoryStockResponseDTO>> searchInventory(
            @RequestParam(required = false) Long variantId,
            @RequestParam(required = false) String locationCode,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                inventoryService.searchInventory(
                        variantId,
                        locationCode,
                        status,
                        page,
                        size
                )
        );
    }

    // ============================================================
    // 4. GET INVENTORY BY ID
    // GET /api/v1/inventory/stock/{inventoryId}
    // ============================================================

    @GetMapping("/stock/{inventoryId}")
    public ResponseEntity<InventoryStockResponseDTO> getInventoryById(
            @PathVariable UUID inventoryId) {

        return ResponseEntity.ok(
                inventoryService.getInventoryById(inventoryId)
        );
    }

    // ============================================================
    // 5. CREATE INITIAL INVENTORY
    // POST /api/v1/inventory/stock
    // ============================================================

    @PostMapping("/stock")
    public ResponseEntity<InventoryStockResponseDTO> createInventory(
            @RequestBody CreateInventoryRequestDTO request) {

        return ResponseEntity.ok(
                inventoryService.createInventory(request)
        );
    }

    // ============================================================
    // 6. UPDATE INVENTORY CONFIGURATION
    // PATCH /api/v1/inventory/stock/{inventoryId}
    // ============================================================

    @PatchMapping("/stock/{inventoryId}")
    public ResponseEntity<InventoryStockResponseDTO> updateInventory(
            @PathVariable UUID inventoryId,
            @RequestBody UpdateInventoryRequestDTO request) {

        return ResponseEntity.ok(
                inventoryService.updateInventory(
                        inventoryId,
                        request
                )
        );
    }

    // ============================================================
    // 7. ADJUST PHYSICAL STOCK
    // POST /api/v1/inventory/stock/{inventoryId}/adjust
    // ============================================================

    @PostMapping("/stock/{inventoryId}/adjust")
    public ResponseEntity<InventoryStockResponseDTO> adjustInventory(
            @PathVariable UUID inventoryId,
            @RequestBody AdjustInventoryRequestDTO request) {

        return ResponseEntity.ok(
                inventoryService.adjustInventory(
                        inventoryId,
                        request
                )
        );
    }
}