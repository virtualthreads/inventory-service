package com.aeropelican.inventoryservice.dto.request;

import jakarta.validation.constraints.Min;
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
public class UpdateInventoryRequestDTO {

    @Min(value = 0, message = "Reorder level cannot be negative")
    private Integer reorderLevel;

    private String status;
}