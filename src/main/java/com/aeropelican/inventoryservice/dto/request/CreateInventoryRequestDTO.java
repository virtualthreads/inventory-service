package com.aeropelican.inventoryservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInventoryRequestDTO {

    private Long productVariantId;

    private String locationCode;

    private Integer quantityOnHand;

    private Integer reorderLevel;
}