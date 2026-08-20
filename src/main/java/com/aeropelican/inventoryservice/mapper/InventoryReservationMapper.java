package com.aeropelican.inventoryservice.mapper;
import com.aeropelican.inventoryservice.dto.response.InventoryReservationResponseDTO;
import com.aeropelican.inventoryservice.entity.InventoryReservation;
import org.springframework.stereotype.Component;

@Component
public class InventoryReservationMapper {

    public InventoryReservationResponseDTO toResponse(
            InventoryReservation reservation
    ) {

        if (reservation == null) {
            return null;
        }

        return InventoryReservationResponseDTO.builder()
                .reservationId(reservation.getReservationId())
                .productVariantId(reservation.getProductVariantId())
                .locationCode(reservation.getLocationCode())
                .orderId(reservation.getOrderId())
                .quantity(reservation.getQuantity())
                .status(reservation.getStatus())
                .expiresAt(reservation.getExpiresAt())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }
}