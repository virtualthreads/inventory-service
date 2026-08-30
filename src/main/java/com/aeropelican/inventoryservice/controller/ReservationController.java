package com.aeropelican.inventoryservice.controller;

import com.aeropelican.inventoryservice.dto.request.CreateReservationRequestDTO;
import com.aeropelican.inventoryservice.dto.response.ReservationResponseDTO;
import com.aeropelican.inventoryservice.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory/reservations")
@RequiredArgsConstructor
@Slf4j
public class ReservationController {
    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponseDTO> createReservation(
            @Valid @RequestBody CreateReservationRequestDTO request) {

        log.info(
                "POST /reservations - Creating reservation for orderId: {}",
                request.getOrderId()
        );

        ReservationResponseDTO response =
                reservationService.createReservation(request);

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponseDTO> getReservation(
            @PathVariable UUID reservationId) {

        log.info(
                "GET /reservations/{} - Fetching reservation",
                reservationId
        );

        ReservationResponseDTO response =
                reservationService.getReservationById(reservationId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{reservationId}/confirm")
    public ResponseEntity<ReservationResponseDTO> confirmReservation(
            @PathVariable UUID reservationId) {

        log.info(
                "POST /reservations/{}/confirm - Confirming reservation",
                reservationId
        );

        ReservationResponseDTO response =
                reservationService.confirmReservation(reservationId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{reservationId}/release")
    public ResponseEntity<Void> releaseReservation(
            @PathVariable UUID reservationId) {

        log.info(
                "POST /reservations/{}/release - Releasing reservation",
                reservationId
        );

        reservationService.releaseReservation(reservationId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/expire")
    public ResponseEntity<List<ReservationResponseDTO>> expireReservations() {

        log.info(
                "POST /reservations/expire - Expiring old reservations"
        );

        List<ReservationResponseDTO> response =
                reservationService.expireReservations();

        return ResponseEntity.ok(response);
    }
}