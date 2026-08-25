package com.aeropelican.inventoryservice.dto.response;

import com.aeropelican.inventoryservice.entity.InventoryReservation.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponseDTO {

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