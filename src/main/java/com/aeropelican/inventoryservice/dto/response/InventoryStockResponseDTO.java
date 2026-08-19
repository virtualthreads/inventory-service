package com.aeropelican.inventoryservice.dto.response;
import com.aeropelican.inventoryservice.enums.InventoryStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryStockResponseDTO {

    private UUID inventoryId;

    private Long productVariantId;

    private String locationCode;

    private Integer quantityOnHand;

    private Integer quantityReserved;

    private Integer availableQuantity;

    private Integer reorderLevel;

    private InventoryStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}