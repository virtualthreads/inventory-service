package com.aeropelican.inventoryservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateReservationRequestDTO {

    @NotNull(message = "Product variant ID is required")
    private Long productVariantId;

    @NotBlank(message = "Location code is required")
    private String locationCode;

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Reservation minutes is required")
    @Min(value = 1, message = "Reservation minutes must be at least 1")
    private Integer reservationMinutes;
}