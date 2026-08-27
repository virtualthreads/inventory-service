package com.aeropelican.inventoryservice.dto.response;

public class InventoryAvailabilityResponse {

    private Long productVariantId;
    private int requestedQuantity;
    private boolean available;
    private int availableQuantity;

    public InventoryAvailabilityResponse(
            Long productVariantId,
            int requestedQuantity,
            boolean available,
            int availableQuantity) {

        this.productVariantId = productVariantId;
        this.requestedQuantity = requestedQuantity;
        this.available = available;
        this.availableQuantity = availableQuantity;
    }

    public Long getProductVariantId() {
        return productVariantId;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public boolean isAvailable() {
        return available;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }
}