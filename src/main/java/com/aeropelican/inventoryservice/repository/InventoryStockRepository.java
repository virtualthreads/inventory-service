package com.aeropelican.inventoryservice.repository;

import com.aeropelican.inventoryservice.entity.InventoryStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryStockRepository
        extends JpaRepository<InventoryStock, UUID> {

    List<InventoryStock> findByProductVariantId(
            Long productVariantId
    );

    Optional<InventoryStock>
    findByProductVariantIdAndLocationCode(
            Long productVariantId,
            String locationCode
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT inventory
            FROM InventoryStock inventory
            WHERE inventory.productVariantId = :productVariantId
              AND inventory.locationCode = :locationCode
            """)
    Optional<InventoryStock>
    findByProductVariantIdAndLocationCodeForUpdate(
            @Param("productVariantId")
            Long productVariantId,

            @Param("locationCode")
            String locationCode
    );

    @Query("""
            SELECT inventory
            FROM InventoryStock inventory
            WHERE (:variantId IS NULL
                   OR inventory.productVariantId = :variantId)
              AND (:locationCode IS NULL
                   OR inventory.locationCode = :locationCode)
              AND (:status IS NULL
                   OR inventory.status = :status)
            """)
    Page<InventoryStock> searchInventory(
            @Param("variantId")
            Long variantId,

            @Param("locationCode")
            String locationCode,

            @Param("status")
            String status,

            Pageable pageable
    );
}