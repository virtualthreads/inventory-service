package com.aeropelican.inventoryservice.entity;
import com.aeropelican.inventoryservice.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "inventory_reservations",
        indexes = {
                @Index(name = "idx_reservation_order",
                        columnList = "order_id"),
                @Index(name = "idx_reservation_variant_status",
                        columnList = "product_variant_id,status"),
                @Index(name = "idx_reservation_expiry",
                        columnList = "status,expires_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(
            name = "reservation_id",
            nullable = false,
            updatable = false,
            length = 36
    )
    private UUID reservationId;


    /**
     * External reference owned by Product Service.
     * No database foreign key.
     */
    @Column(
            name = "product_variant_id",
            nullable = false
    )
    private Long productVariantId;
    @Column(name = "location_code",
            nullable = false,
            length = 30)
    private String locationCode;
    @Column(name = "order_id",
            nullable = false,
            length = 100)
    private String orderId;
    @Column(name = "quantity",
            nullable = false)
    private Integer quantity;
    @Enumerated(EnumType.STRING)
    @Column(name = "status",
            nullable = false,
            length = 20)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.RESERVED;
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    @Column(name = "created_at",
            nullable = false,
            updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at",
            nullable = false)
    private LocalDateTime updatedAt;


    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = ReservationStatus.RESERVED;
        }
    }


    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}