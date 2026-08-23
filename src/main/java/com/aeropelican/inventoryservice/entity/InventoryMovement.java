package com.aeropelican.inventoryservice.entity;

import com.aeropelican.inventoryservice.enums.MovementType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "inventory_movements",
        indexes = {
                @Index(
                        name = "idx_movement_variant_location",
                        columnList = "product_variant_id,location_code"
                ),
                @Index(
                        name = "idx_movement_reference",
                        columnList = "reference_id"
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
    @Column(
            name = "movement_id",
            nullable = false,
            updatable = false
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
            length = 50
    )
    private String referenceType;


    @Column(
            name = "reference_id",
            length = 100
    )
    private String referenceId;


    @Column(
            name = "notes",
            length = 500
    )
    private String notes;


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