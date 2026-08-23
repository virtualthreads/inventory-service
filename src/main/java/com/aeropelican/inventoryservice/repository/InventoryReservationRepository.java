package com.aeropelican.inventoryservice.repository;

import com.aeropelican.inventoryservice.entity.InventoryReservation;
import com.aeropelican.inventoryservice.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface InventoryReservationRepository
        extends JpaRepository<InventoryReservation, UUID> {

    List<InventoryReservation> findByOrderId(
            String orderId
    );

    List<InventoryReservation>
    findByProductVariantIdAndStatus(
            Long productVariantId,
            ReservationStatus status
    );

    List<InventoryReservation>
    findByStatusAndExpiresAtBefore(
            ReservationStatus status,
            LocalDateTime expiresAt
    );
}