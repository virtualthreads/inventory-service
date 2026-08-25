package com.aeropelican.inventoryservice.controller;
import com.aeropelican.inventoryservice.dto.response.APIResponse;
import com.aeropelican.inventoryservice.dto.response.InventoryMovementResponseDTO;
import com.aeropelican.inventoryservice.service.InventoryMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory/variants")
@RequiredArgsConstructor
public class InventoryMovementController {

    private final InventoryMovementService movementService;


    // ============================================================
    // GET MOVEMENT HISTORY
    // ============================================================

    @GetMapping("/{variantId}/movements")
    public ResponseEntity<APIResponse<Page<InventoryMovementResponseDTO>>> getByVariant(
            @PathVariable Long variantId,

            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        Page<InventoryMovementResponseDTO> response =
                movementService.getByVariant(
                        variantId,
                        pageable
                );

        return ResponseEntity.ok(
                APIResponse.<Page<InventoryMovementResponseDTO>>builder()
                        .data(response)
                        .message("Inventory movements fetched successfully")
                        .success(true)
                        .build()
        );
    }
}
