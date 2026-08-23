package com.aeropelican.inventoryservice.repository;

import com.aeropelican.inventoryservice.entity.InventoryStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InventoryStockRepository
        extends JpaRepository<InventoryStock, UUID> {

    Optional<InventoryStock> findByProductVariantIdAndLocationCode(
            Long productVariantId,
            String locationCode
    );

    boolean existsByProductVariantIdAndLocationCode(
            Long productVariantId,
            String locationCode
    );
}