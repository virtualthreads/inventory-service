package com.aeropelican.inventoryservice.repository;

import com.aeropelican.inventoryservice.entity.InventoryStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface InventoryStockRepository
        extends JpaRepository<InventoryStock, UUID> {

    Optional<InventoryStock> findByProductVariantIdAndLocationCode(
            Long productVariantId,
            String locationCode
    );

    Page<InventoryStock> findByProductVariantId(
            Long productVariantId,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT i
            FROM InventoryStock i
            WHERE i.inventoryId = :inventoryId
            """)
    Optional<InventoryStock> findByIdForUpdate(
            @Param("inventoryId") UUID inventoryId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT i
            FROM InventoryStock i
            WHERE i.productVariantId = :productVariantId
              AND i.locationCode = :locationCode
            """)
    Optional<InventoryStock> findByVariantAndLocationForUpdate(
            @Param("productVariantId") Long productVariantId,
            @Param("locationCode") String locationCode
    );

    boolean existsByProductVariantIdAndLocationCode(
            Long productVariantId,
            String locationCode
    );
}