package com.aeropelican.inventoryservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
                        columnNames = {"product_variant_id", "location_code"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStock {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "inventory_id", length = 36, nullable = false)
    private UUID inventoryId;

    @Column(name = "product_variant_id", nullable = false)
    private Long productVariantId;

    @Column(name = "location_code", length = 30, nullable = false)
    private String locationCode;

    @Column(name = "quantity_on_hand", nullable = false)
    private Integer quantityOnHand;

    @Column(name = "quantity_reserved", nullable = false)
    private Integer quantityReserved;

    @Column(name = "reorder_level", nullable = false)
    private Integer reorderLevel;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (inventoryId == null) {
            inventoryId = UUID.randomUUID();
        }

        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

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
            status = "OUT_OF_STOCK";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}