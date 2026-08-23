package com.aeropelican.inventoryservice.controller;

import com.aeropelican.inventoryservice.dto.request.InventoryMovementRequest;
import com.aeropelican.inventoryservice.dto.response.InventoryMovementResponse;
import com.aeropelican.inventoryservice.service.InventoryMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory/movements")
@RequiredArgsConstructor
public class InventoryMovementController {

    private final InventoryMovementService inventoryMovementService;


    // ============================================================
    // CREATE MOVEMENT
    // ============================================================

    @PostMapping
    public ResponseEntity<InventoryMovementResponse> createMovement(
            @Valid @RequestBody InventoryMovementRequest request) {

        InventoryMovementResponse response =
                inventoryMovementService.createMovement(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // ============================================================
    // GET MOVEMENT BY ID
    // ============================================================

    @GetMapping("/{movementId}")
    public ResponseEntity<InventoryMovementResponse> getMovementById(
            @PathVariable UUID movementId) {

        InventoryMovementResponse response =
                inventoryMovementService.getMovementById(movementId);

        return ResponseEntity.ok(response);
    }
}