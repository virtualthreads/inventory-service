package com.aeropelican.inventoryservice.entity;
import com.aeropelican.inventoryservice.enums.MovementType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(
        name = "inventory_movements",
        indexes = {
                @Index(
                        name = "idx_movement_variant",
                        columnList = "product_variant_id"
                ),
                @Index(
                        name = "idx_movement_reference",
                        columnList = "reference_type,reference_id"
                ),
                @Index(
                        name = "idx_movement_created",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(
            name = "movement_id",
            nullable = false,
            updatable = false,
            length = 36
    )
    private UUID movementId;
    @Column(
            name = "product_variant_id",
            nullable = false
    )
    private Long productVariantId;
    @Column(
            name = "location_code",
            nullable = false,
            length = 30
    )
    private String locationCode;
    @Enumerated(EnumType.STRING)
    @Column(
            name = "movement_type",
            nullable = false,
            length = 30
    )
    private MovementType movementType;
    @Column(
            name = "quantity",
            nullable = false
    )
    private Integer quantity;
    @Column(
            name = "reference_type",
            length = 30
    )
    private String referenceType;
    @Column(
            name = "reference_id",
            length = 100
    )
    private String referenceId;
    @Column(
            name = "reason",
            length = 255
    )
    private String reason;
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {

        createdAt = LocalDateTime.now();
    }
}