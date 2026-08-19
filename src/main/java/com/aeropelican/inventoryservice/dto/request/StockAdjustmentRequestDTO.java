package com.aeropelican.inventoryservice.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockAdjustmentRequestDTO {

    @NotNull(message = "Adjustment quantity is required")
    @Positive(message = "Adjustment quantity must be greater than zero")
    private Integer quantity;

    @NotBlank(message = "Adjustment type is required")
    private String adjustmentType;

    @Size(max = 255, message = "Reason must not exceed 255 characters")
    private String reason;
}
