package com.aeropelican.inventoryservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponseDTO {

    private Long productVariantId;

    private Integer totalQuantityOnHand;

    private Integer totalQuantityReserved;

    private Integer totalAvailableQuantity;

    private List<InventoryLocationResponseDTO> locations;
}