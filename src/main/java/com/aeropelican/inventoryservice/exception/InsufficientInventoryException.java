package com.aeropelican.inventoryservice.exception;

public class InsufficientInventoryException
        extends RuntimeException {

    public InsufficientInventoryException(
            Long productVariantId,
            String locationCode) {

        super(
                "Insufficient inventory for product variant "
                        + productVariantId
                        + " at location "
                        + locationCode
        );
    }
}