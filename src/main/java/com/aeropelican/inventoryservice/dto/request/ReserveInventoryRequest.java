package com.aeropelican.inventoryservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReserveInventoryRequest {

    @NotNull
    private Long productVariantId;

    @NotBlank
    private String locationCode;

    @NotBlank
    private String orderId;

    @NotNull
    @Min(1)
    private Integer quantity;

    private LocalDateTime expiresAt;
}