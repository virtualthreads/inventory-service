package com.aeropelican.inventoryservice.service;

import com.aeropelican.inventoryservice.dto.request.ReserveInventoryRequest;
import com.aeropelican.inventoryservice.dto.response.InventoryReservationResponse;

import java.util.UUID;

public interface InventoryReservationService {

    InventoryReservationResponse reserve(
            ReserveInventoryRequest request
    );

    InventoryReservationResponse release(
            UUID reservationId
    );

    InventoryReservationResponse complete(
            UUID reservationId
    );
}