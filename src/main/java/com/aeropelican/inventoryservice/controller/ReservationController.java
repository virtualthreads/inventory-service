package com.aeropelican.inventoryservice.controller;

import com.aeropelican.inventoryservice.dto.request.ReserveInventoryRequest;
import com.aeropelican.inventoryservice.dto.response.APIResponse;
import com.aeropelican.inventoryservice.dto.response.InventoryReservationResponse;
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
public class ReservationController {

    private final InventoryReservationService reservationService;


    @PostMapping
    public ResponseEntity<
            APIResponse<InventoryReservationResponse>
            > reserve(
            @Valid @RequestBody ReserveInventoryRequest request) {

        InventoryReservationResponse response =
                reservationService.reserve(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        APIResponse
                                .<InventoryReservationResponse>builder()
                                .data(response)
                                .message(
                                        "Inventory reserved successfully"
                                )
                                .success(true)
                                .build()
                );
    }


    @PostMapping("/{reservationId}/release")
    public ResponseEntity<
            APIResponse<InventoryReservationResponse>
            > release(
            @PathVariable UUID reservationId) {

        InventoryReservationResponse response =
                reservationService.release(
                        reservationId
                );

        return ResponseEntity.ok(
                APIResponse
                        .<InventoryReservationResponse>builder()
                        .data(response)
                        .message(
                                "Reservation released successfully"
                        )
                        .success(true)
                        .build()
        );
    }


    @PostMapping("/{reservationId}/complete")
    public ResponseEntity<
            APIResponse<InventoryReservationResponse>
            > complete(
            @PathVariable UUID reservationId) {

        InventoryReservationResponse response =
                reservationService.complete(
                        reservationId
                );

        return ResponseEntity.ok(
                APIResponse
                        .<InventoryReservationResponse>builder()
                        .data(response)
                        .message(
                                "Reservation completed successfully"
                        )
                        .success(true)
                        .build()
        );
    }
}