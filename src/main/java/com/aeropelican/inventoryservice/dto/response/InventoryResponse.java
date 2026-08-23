package com.aeropelican.inventoryservice.dto.response;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private String sku;
    private Integer totalQuantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
}
