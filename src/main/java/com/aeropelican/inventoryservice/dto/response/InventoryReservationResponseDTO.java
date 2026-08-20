package com.aeropelican.inventoryservice.dto.response;
import com.aeropelican.inventoryservice.enums.ReservationStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReservationResponseDTO {

    private UUID reservationId;

    private Long productVariantId;

    private String locationCode;

    private String orderId;

    private Integer quantity;

    private ReservationStatus status;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}