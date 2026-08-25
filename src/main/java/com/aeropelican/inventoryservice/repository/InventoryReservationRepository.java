package com.aeropelican.inventoryservice.repository;

import com.aeropelican.inventoryservice.entity.InventoryReservation;
import com.aeropelican.inventoryservice.entity.InventoryReservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryReservationRepository
        extends JpaRepository<InventoryReservation, UUID> {

    List<InventoryReservation> findByStatusAndExpiresAtBefore(
            ReservationStatus status,
            LocalDateTime expiresAt
    );

    List<InventoryReservation> findByOrderId(
            String orderId
    );

    List<InventoryReservation> findByProductVariantIdAndStatus(
            Long productVariantId,
            ReservationStatus status
    );
}