package com.aeropelican.inventoryservice.repository;

import com.aeropelican.inventoryservice.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InventoryMovementRepository
        extends JpaRepository<InventoryMovement, UUID> {
}