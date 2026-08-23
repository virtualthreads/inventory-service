package com.aeropelican.inventoryservice.entity;

import com.aeropelican.inventoryservice.enums.InventoryStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "inventory_stock",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_inventory_variant_location",
                        columnNames = {
                                "product_variant_id",
                                "location_code"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_inventory_variant",
                        columnList = "product_variant_id"
                ),
                @Index(
                        name = "idx_inventory_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryStock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(
            name = "inventory_id",
            nullable = false,
            updatable = false,
            length = 36
    )
    private UUID inventoryId;

    /**
     * Product Service owns product_variant_id.
     *
     * This is intentionally NOT a @ManyToOne relationship
     * and does NOT have a database foreign key.
     */
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

    @Column(
            name = "quantity_on_hand",
            nullable = false
    )
    @Builder.Default
    private Integer quantityOnHand = 0;

    @Column(
            name = "quantity_reserved",
            nullable = false
    )
    @Builder.Default
    private Integer quantityReserved = 0;

    @Column(
            name = "reorder_level",
            nullable = false
    )
    @Builder.Default
    private Integer reorderLevel = 10;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private InventoryStatus status = InventoryStatus.OUT_OF_STOCK;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (quantityOnHand == null) {
            quantityOnHand = 0;
        }

        if (quantityReserved == null) {
            quantityReserved = 0;
        }

        if (reorderLevel == null) {
            reorderLevel = 10;
        }

        if (status == null) {
            status = InventoryStatus.OUT_OF_STOCK;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Transient
    public Integer getAvailableQuantity() {
        return quantityOnHand - quantityReserved;
    }
}