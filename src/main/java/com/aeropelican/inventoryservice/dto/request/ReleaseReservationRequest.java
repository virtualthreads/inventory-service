package com.aeropelican.inventoryservice.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReleaseReservationRequest {

    @NotNull
    private Long reservationId;
}