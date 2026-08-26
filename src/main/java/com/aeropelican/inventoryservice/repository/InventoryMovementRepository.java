package com.aeropelican.inventoryservice.repository;

import com.aeropelican.inventoryservice.entity.InventoryMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface InventoryMovementRepository
    extends JpaRepository<InventoryMovement, UUID> {

    Page<InventoryMovement> findByProductVariantId(
        Long productVariantId,
        Pageable pageable
    );
}