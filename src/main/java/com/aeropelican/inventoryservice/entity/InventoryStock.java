package com.aeropelican.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "inventory_stock",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_stock_variant_location",
                        columnNames = {
                                "product_variant_id",
                                "location_code"
                        }
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
    @Column(
            name = "stock_id",
            nullable = false,
            updatable = false
    )
    private UUID stockId;

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
            name = "total_quantity",
            nullable = false
    )
    private Integer totalQuantity;

    @Column(
            name = "reserved_quantity",
            nullable = false
    )
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(
            name = "available_quantity",
            nullable = false
    )
    private Integer availableQuantity;
}