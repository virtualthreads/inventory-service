package com.aeropelican.inventoryservice.model;

public class InventoryStock {

    private String inventoryId;
    private Long productVariantId;
    private String locationCode;
    private Integer quantityOnHand;
    private Integer quantityReserved;
    private Integer reorderLevel;
    private String status;

    public InventoryStock() {
    }

    public InventoryStock(
            String inventoryId,
            Long productVariantId,
            String locationCode,
            Integer quantityOnHand,
            Integer quantityReserved,
            Integer reorderLevel,
            String status
    ) {
        this.inventoryId = inventoryId;
        this.productVariantId = productVariantId;
        this.locationCode = locationCode;
        this.quantityOnHand = quantityOnHand;
        this.quantityReserved = quantityReserved;
        this.reorderLevel = reorderLevel;
        this.status = status;
    }

    public String getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(String inventoryId) {
        this.inventoryId = inventoryId;
    }

    public Long getProductVariantId() {
        return productVariantId;
    }

    public void setProductVariantId(Long productVariantId) {
        this.productVariantId = productVariantId;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public Integer getQuantityOnHand() {
        return quantityOnHand;
    }

    public void setQuantityOnHand(Integer quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
    }

    public Integer getQuantityReserved() {
        return quantityReserved;
    }

    public void setQuantityReserved(Integer quantityReserved) {
        this.quantityReserved = quantityReserved;
    }

    public Integer getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(Integer reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}