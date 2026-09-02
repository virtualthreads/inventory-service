package com.aeropelican.inventoryservice.controller;

import com.aeropelican.inventoryservice.dto.request.AdjustInventoryRequestDTO;
import com.aeropelican.inventoryservice.dto.request.CreateInventoryRequestDTO;
import com.aeropelican.inventoryservice.dto.request.UpdateInventoryRequestDTO;
import com.aeropelican.inventoryservice.dto.response.InventoryAvailabilityResponseDTO;
import com.aeropelican.inventoryservice.dto.response.InventoryResponseDTO;
import com.aeropelican.inventoryservice.dto.response.InventoryStockResponseDTO;
import com.aeropelican.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;


    @GetMapping("/variants/{variantId}")
    public ResponseEntity<InventoryResponseDTO> getInventoryByVariantId(
            @PathVariable Long variantId) {

        return ResponseEntity.ok(
                inventoryService.getInventoryByVariantId(variantId)
        );
    }


    @GetMapping("/variants/{variantId}/availability")
    public ResponseEntity<InventoryAvailabilityResponseDTO> checkAvailability(
            @PathVariable Long variantId,
            @RequestParam(defaultValue = "1") Integer quantity) {

        return ResponseEntity.ok(
                inventoryService.checkAvailability(
                        variantId,
                        quantity
                )
        );
    }
    @GetMapping("/stock")
    public ResponseEntity<Page<InventoryStockResponseDTO>> searchInventory(
            @RequestParam(required = false) Long variantId,

            @RequestParam(required = false)
            String locationCode,

            @RequestParam(required = false)
            String status,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size) {

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

    @GetMapping("/stock/{inventoryId}")
    public ResponseEntity<InventoryStockResponseDTO> getInventoryById(
            @PathVariable UUID inventoryId) {

        return ResponseEntity.ok(
                inventoryService.getInventoryById(inventoryId)
        );
    }

    @PostMapping("/stock")
    public ResponseEntity<InventoryStockResponseDTO> createInventory(
            @Valid @RequestBody CreateInventoryRequestDTO request) {

        InventoryStockResponseDTO response =
                inventoryService.createInventory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @PatchMapping("/stock/{inventoryId}")
    public ResponseEntity<InventoryStockResponseDTO> updateInventory(
            @PathVariable UUID inventoryId,
            @Valid @RequestBody UpdateInventoryRequestDTO request) {

        return ResponseEntity.ok(
                inventoryService.updateInventory(
                        inventoryId,
                        request
                )
        );
    }

    @PostMapping("/stock/{inventoryId}/adjust")
    public ResponseEntity<InventoryStockResponseDTO> adjustInventory(
            @PathVariable UUID inventoryId,
            @Valid @RequestBody AdjustInventoryRequestDTO request) {

        return ResponseEntity.ok(
                inventoryService.adjustInventory(
                        inventoryId,
                        request
                )
        );
    }
}