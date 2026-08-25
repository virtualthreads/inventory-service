package com.aeropelican.inventoryservice.dto.response;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationExpirationResponseDTO {

    private Integer expiredReservations;

    private String message;
}