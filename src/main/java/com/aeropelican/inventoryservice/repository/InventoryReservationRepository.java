package com.aeropelican.inventoryservice.repository;
import com.aeropelican.inventoryservice.entity.InventoryReservation;
import com.aeropelican.inventoryservice.enums.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface InventoryReservationRepository
        extends JpaRepository<InventoryReservation, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r
            FROM InventoryReservation r
            WHERE r.reservationId = :reservationId
            """)
    java.util.Optional<InventoryReservation> findByIdForUpdate(
            @Param("reservationId") UUID reservationId
    );

    List<InventoryReservation> findByStatusAndExpiresAtBefore(
            ReservationStatus status,
            LocalDateTime expiresAt
    );
}