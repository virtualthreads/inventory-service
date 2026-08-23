package com.aeropelican.inventoryservice.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityResponseDTO {

    private Long productVariantId;

    private String locationCode;

    private Integer totalQuantity;

    private Integer reservedQuantity;

    private Integer availableQuantity;

    private boolean available;
}