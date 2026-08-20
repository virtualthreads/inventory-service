package com.aeropelican.inventoryservice.dto.response;
import com.aeropelican.inventoryservice.enums.MovementType;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovementResponseDTO {

    private UUID movementId;

    private Long productVariantId;

    private String locationCode;

    private MovementType movementType;

    private Integer quantity;

    private String referenceType;

    private String referenceId;

    private String reason;

    private LocalDateTime createdAt;
}
