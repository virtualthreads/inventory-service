package com.aeropelican.inventoryservice.mapper;

import com.aeropelican.inventoryservice.dto.response.InventoryReservationResponse;
import com.aeropelican.inventoryservice.entity.InventoryReservation;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public InventoryReservationResponse toResponse(
            InventoryReservation reservation) {

        if (reservation == null) {
            return null;
        }

        return InventoryReservationResponse.builder()
                .reservationId(
                        reservation.getReservationId()
                )
                .productVariantId(
                        reservation.getProductVariantId()
                )
                .locationCode(
                        reservation.getLocationCode()
                )
                .orderId(
                        reservation.getOrderId()
                )
                .quantity(
                        reservation.getQuantity()
                )
                .status(
                        reservation.getStatus()
                )
                .expiresAt(
                        reservation.getExpiresAt()
                )
                .createdAt(
                        reservation.getCreatedAt()
                )
                .updatedAt(
                        reservation.getUpdatedAt()
                )
                .build();
    }
}