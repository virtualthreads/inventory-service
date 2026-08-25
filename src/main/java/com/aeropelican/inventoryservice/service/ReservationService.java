package com.aeropelican.inventoryservice.service;

import com.aeropelican.inventoryservice.dto.request.CreateReservationRequestDTO;
import com.aeropelican.inventoryservice.dto.response.ReservationResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ReservationService {

    ReservationResponseDTO createReservation(
            CreateReservationRequestDTO request
    );

    ReservationResponseDTO getReservationById(
            UUID reservationId
    );

    ReservationResponseDTO confirmReservation(
            UUID reservationId
    );

    void releaseReservation(
            UUID reservationId
    );

    List<ReservationResponseDTO> expireReservations();
}