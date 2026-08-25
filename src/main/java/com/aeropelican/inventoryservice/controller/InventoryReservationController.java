package com.aeropelican.inventoryservice.controller;

import com.aeropelican.inventoryservice.dto.request.CreateReservationRequestDTO;
import com.aeropelican.inventoryservice.dto.response.APIResponse;
import com.aeropelican.inventoryservice.dto.response.InventoryReservationResponseDTO;
import com.aeropelican.inventoryservice.dto.response.ReservationExpirationResponseDTO;
import com.aeropelican.inventoryservice.service.InventoryReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory/reservations")
@RequiredArgsConstructor
public class InventoryReservationController {

    private final InventoryReservationService reservationService;


    // ============================================================
    // CREATE RESERVATION
    // ============================================================

    @PostMapping
    public ResponseEntity<APIResponse<InventoryReservationResponseDTO>> reserve(
            @Valid @RequestBody CreateReservationRequestDTO request
    ) {

        InventoryReservationResponseDTO response =
                reservationService.reserve(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        APIResponse.<InventoryReservationResponseDTO>builder()
                                .data(response)
                                .message("Inventory reserved successfully")
                                .success(true)
                                .build()
                );
    }


    // ============================================================
    // GET RESERVATION BY ID
    // ============================================================

    @GetMapping("/{reservationId}")
    public ResponseEntity<APIResponse<InventoryReservationResponseDTO>> getById(
            @PathVariable UUID reservationId
    ) {

        InventoryReservationResponseDTO response =
                reservationService.getById(reservationId);

        return ResponseEntity.ok(
                APIResponse.<InventoryReservationResponseDTO>builder()
                        .data(response)
                        .message("Reservation fetched successfully")
                        .success(true)
                        .build()
        );
    }


    // ============================================================
    // CONFIRM RESERVATION
    // ============================================================

    @PostMapping("/{reservationId}/confirm")
    public ResponseEntity<APIResponse<InventoryReservationResponseDTO>> confirm(
            @PathVariable UUID reservationId
    ) {

        InventoryReservationResponseDTO response =
                reservationService.confirm(reservationId);

        return ResponseEntity.ok(
                APIResponse.<InventoryReservationResponseDTO>builder()
                        .data(response)
                        .message("Reservation confirmed successfully")
                        .success(true)
                        .build()
        );
    }


    // ============================================================
    // RELEASE RESERVATION
    // ============================================================

    @PostMapping("/{reservationId}/release")
    public ResponseEntity<Void> release(
            @PathVariable UUID reservationId
    ) {

        reservationService.release(reservationId);

        return ResponseEntity.noContent().build();
    }


    // ============================================================
    // EXPIRE RESERVATIONS
    // ============================================================

    @PostMapping("/expire")
    public ResponseEntity<APIResponse<ReservationExpirationResponseDTO>> expireReservations() {

        ReservationExpirationResponseDTO response =
                reservationService.expireReservations();

        return ResponseEntity.ok(
                APIResponse.<ReservationExpirationResponseDTO>builder()
                        .data(response)
                        .message("Reservation expiration completed successfully")
                        .success(true)
                        .build()
        );
    }
}