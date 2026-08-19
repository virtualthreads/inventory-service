package com.aeropelican.inventoryservice.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReservationRequestDTO {

    @NotNull(message = "Product variant ID is required")
    private Long productVariantId;

    @NotBlank(message = "Location code is required")
    @Size(max = 30, message = "Location code must not exceed 30 characters")
    private String locationCode;

    @NotBlank(message = "Order ID is required")
    @Size(max = 100, message = "Order ID must not exceed 100 characters")
    private String orderId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Reservation quantity must be greater than zero")
    private Integer quantity;

    @NotNull(message = "Expiration time is required")
    @Future(message = "Expiration time must be in the future")
    private LocalDateTime expiresAt;
}
