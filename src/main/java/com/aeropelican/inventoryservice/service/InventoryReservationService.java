package com.aeropelican.inventoryservice.service;
import com.aeropelican.inventoryservice.dto.request.CreateReservationRequestDTO;
import com.aeropelican.inventoryservice.dto.response.InventoryReservationResponseDTO;
import com.aeropelican.inventoryservice.dto.response.ReservationExpirationResponseDTO;
import java.util.UUID;

public interface InventoryReservationService {

    InventoryReservationResponseDTO reserve(
        CreateReservationRequestDTO request
    );

    InventoryReservationResponseDTO getById(
        UUID reservationId
    );

    InventoryReservationResponseDTO confirm(
        UUID reservationId
    );

    InventoryReservationResponseDTO release(
        UUID reservationId
    );
    ReservationExpirationResponseDTO expireReservations();
}