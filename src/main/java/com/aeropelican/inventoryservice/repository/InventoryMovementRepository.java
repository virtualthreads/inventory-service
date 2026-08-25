package com.aeropelican.inventoryservice.repository;

import com.aeropelican.inventoryservice.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryMovementRepository
        extends JpaRepository<InventoryMovement, UUID> {

    List<InventoryMovement> findByProductVariantId(
            Long productVariantId
    );

    List<InventoryMovement> findByProductVariantIdAndLocationCode(
            Long productVariantId,
            String locationCode
    );
}