package com.aeropelican.inventoryservice.controller;

import com.aeropelican.inventoryservice.entity.InventoryMovement;
import com.aeropelican.inventoryservice.repository.InventoryMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryMovementController {
    private final InventoryMovementRepository inventoryMovementRepository;
    @GetMapping("/variants/{variantId}/movements")
    public ResponseEntity<Page<InventoryMovement>> getMovementHistory(

            @PathVariable Long variantId,

            @RequestParam(required = false)
            String locationCode,

            @RequestParam(required = false)
            InventoryMovement.MovementType movementType,

            @RequestParam(required = false)
            LocalDateTime from,

            @RequestParam(required = false)
            LocalDateTime to,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size
    ) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must be greater than or equal to zero"
            );
        }

        if (size < 1) {
            throw new IllegalArgumentException(
                    "Size must be greater than zero"
            );
        }
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );
        List<InventoryMovement> movements =
                inventoryMovementRepository
                        .findByProductVariantId(variantId);
        if (locationCode != null
                && !locationCode.isBlank()) {

            String requestedLocation =
                    locationCode.trim();

            movements = movements.stream()
                    .filter(movement ->
                            movement.getLocationCode() != null
                                    && movement.getLocationCode()
                                    .equalsIgnoreCase(
                                            requestedLocation
                                    )
                    )
                    .toList();
        }
        if (movementType != null) {

            movements = movements.stream()
                    .filter(movement ->
                            movement.getMovementType() != null
                                    && movement.getMovementType()
                                    == movementType
                    )
                    .toList();
        }
        if (from != null) {

            movements = movements.stream()
                    .filter(movement ->
                            movement.getCreatedAt() != null
                                    && !movement.getCreatedAt()
                                    .isBefore(from)
                    )
                    .toList();
        }
        if (to != null) {

            movements = movements.stream()
                    .filter(movement ->
                            movement.getCreatedAt() != null
                                    && !movement.getCreatedAt()
                                    .isAfter(to)
                    )
                    .toList();
        }
        movements = movements.stream()
                .sorted(
                        (first, second) -> {

                            LocalDateTime firstDate =
                                    first.getCreatedAt();

                            LocalDateTime secondDate =
                                    second.getCreatedAt();

                            if (firstDate == null
                                    && secondDate == null) {
                                return 0;
                            }

                            if (firstDate == null) {
                                return 1;
                            }

                            if (secondDate == null) {
                                return -1;
                            }

                            return secondDate.compareTo(
                                    firstDate
                            );
                        }
                )
                .toList();
        int start =
                (int) pageable.getOffset();

        int end =
                Math.min(
                        start + pageable.getPageSize(),
                        movements.size()
                );

        List<InventoryMovement> pageContent;

        if (start >= movements.size()) {

            pageContent = List.of();

        } else {

            pageContent =
                    movements.subList(start, end);
        }
        Page<InventoryMovement> result =
                new PageImpl<>(
                        pageContent,
                        pageable,
                        movements.size()
                );

        return ResponseEntity.ok(result);
    }
}