package com.aeropelican.inventoryservice.exception;

import java.util.UUID;

public class MovementNotFoundException
        extends RuntimeException {

    public MovementNotFoundException(UUID movementId) {

        super(
                "Inventory movement not found: "
                        + movementId
        );
    }
}