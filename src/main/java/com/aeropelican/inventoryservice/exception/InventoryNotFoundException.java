package com.aeropelican.inventoryservice.exception;

public class InventoryNotFoundException
        extends RuntimeException {

    public InventoryNotFoundException(
            Long productVariantId,
            String locationCode) {

        super(
                "Inventory not found for product variant "
                        + productVariantId
                        + " at location "
                        + locationCode
        );
    }
}