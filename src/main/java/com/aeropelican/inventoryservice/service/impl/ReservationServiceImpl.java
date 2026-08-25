package com.aeropelican.inventoryservice.service.impl;

import com.aeropelican.inventoryservice.dto.request.CreateReservationRequestDTO;
import com.aeropelican.inventoryservice.dto.response.ReservationResponseDTO;
import com.aeropelican.inventoryservice.entity.InventoryMovement;
import com.aeropelican.inventoryservice.entity.InventoryReservation;
import com.aeropelican.inventoryservice.entity.InventoryReservation.ReservationStatus;
import com.aeropelican.inventoryservice.entity.InventoryStock;
import com.aeropelican.inventoryservice.exception.InsufficientInventoryException;
import com.aeropelican.inventoryservice.exception.InventoryNotFoundException;
import com.aeropelican.inventoryservice.repository.InventoryMovementRepository;
import com.aeropelican.inventoryservice.repository.InventoryReservationRepository;
import com.aeropelican.inventoryservice.repository.InventoryStockRepository;
import com.aeropelican.inventoryservice.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private final InventoryReservationRepository inventoryReservationRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    // =========================================================
    // CREATE RESERVATION
    // =========================================================

    @Override
    @Transactional
    public ReservationResponseDTO createReservation(
            CreateReservationRequestDTO request) {

        log.info(
                "Creating reservation - variantId: {}, location: {}, orderId: {}, quantity: {}",
                request.getProductVariantId(),
                request.getLocationCode(),
                request.getOrderId(),
                request.getQuantity()
        );

        /*
         * Lock the inventory row.
         *
         * This prevents two concurrent requests from
         * reserving the same available stock.
         */
        InventoryStock inventoryStock =
                inventoryStockRepository
                        .findByProductVariantIdAndLocationCodeForUpdate(
                                request.getProductVariantId(),
                                request.getLocationCode()
                        )
                        .orElseThrow(() -> {

                            log.error(
                                    "Inventory not found for variantId: {} and location: {}",
                                    request.getProductVariantId(),
                                    request.getLocationCode()
                            );

                            return new InventoryNotFoundException(
                                    "Inventory not found for product variant "
                                            + request.getProductVariantId()
                                            + " at location "
                                            + request.getLocationCode()
                            );
                        });

        // ---------------------------------------------------------
        // Calculate available quantity
        // ---------------------------------------------------------

        int quantityOnHand =
                inventoryStock.getQuantityOnHand();

        int quantityReserved =
                inventoryStock.getQuantityReserved();

        int availableQuantity =
                quantityOnHand - quantityReserved;

        log.info(
                "Available quantity: {}",
                availableQuantity
        );

        // ---------------------------------------------------------
        // Validate available stock
        // ---------------------------------------------------------

        if (availableQuantity < request.getQuantity()) {

            log.error(
                    "Insufficient inventory. Requested: {}, available: {}",
                    request.getQuantity(),
                    availableQuantity
            );

            throw new InsufficientInventoryException(
                    "Insufficient inventory. Available quantity: "
                            + availableQuantity
                            + ", requested quantity: "
                            + request.getQuantity()
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        LocalDateTime expiresAt =
                now.plusMinutes(
                        request.getReservationMinutes()
                );

        // ---------------------------------------------------------
        // Create reservation
        // ---------------------------------------------------------

        InventoryReservation reservation =
                InventoryReservation.builder()
                        .reservationId(UUID.randomUUID())
                        .productVariantId(
                                request.getProductVariantId()
                        )
                        .locationCode(
                                request.getLocationCode()
                        )
                        .orderId(
                                request.getOrderId()
                        )
                        .quantity(
                                request.getQuantity()
                        )
                        .status(
                                ReservationStatus.RESERVED
                        )
                        .expiresAt(
                                expiresAt
                        )
                        .createdAt(
                                now
                        )
                        .updatedAt(
                                now
                        )
                        .build();

        // ---------------------------------------------------------
        // Increase reserved quantity
        //
        // quantity_on_hand DOES NOT change here.
        // ---------------------------------------------------------

        inventoryStock.setQuantityReserved(
                inventoryStock.getQuantityReserved()
                        + request.getQuantity()
        );

        inventoryStock.setUpdatedAt(now);

        inventoryStockRepository.save(
                inventoryStock
        );

        // ---------------------------------------------------------
        // Save reservation
        // ---------------------------------------------------------

        InventoryReservation savedReservation =
                inventoryReservationRepository.save(
                        reservation
                );

        // ---------------------------------------------------------
        // Create RESERVATION movement
        // ---------------------------------------------------------

        InventoryMovement movement =
                InventoryMovement.builder()
                        .movementId(UUID.randomUUID())
                        .productVariantId(
                                request.getProductVariantId()
                        )
                        .locationCode(
                                request.getLocationCode()
                        )
                        .movementType(
                                InventoryMovement.MovementType.RESERVATION
                        )
                        .quantity(
                                request.getQuantity()
                        )
                        .referenceType(
                                "ORDER"
                        )
                        .referenceId(
                                request.getOrderId()
                        )
                        .reason(
                                "Stock reserved for order"
                        )
                        .createdAt(
                                now
                        )
                        .build();

        inventoryMovementRepository.save(
                movement
        );

        log.info(
                "Reservation created successfully. Reservation ID: {}",
                savedReservation.getReservationId()
        );

        return toResponse(
                savedReservation
        );
    }

    // =========================================================
    // GET RESERVATION
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public ReservationResponseDTO getReservationById(
            UUID reservationId) {

        log.info(
                "Fetching reservation: {}",
                reservationId
        );

        InventoryReservation reservation =
                inventoryReservationRepository
                        .findById(reservationId)
                        .orElseThrow(() -> {

                            log.error(
                                    "Reservation not found: {}",
                                    reservationId
                            );

                            return new InventoryNotFoundException(
                                    "Reservation not found with id: "
                                            + reservationId
                            );
                        });

        return toResponse(
                reservation
        );
    }

    // =========================================================
    // CONFIRM RESERVATION
    // =========================================================

    @Override
    @Transactional
    public ReservationResponseDTO confirmReservation(
            UUID reservationId) {

        log.info(
                "Confirming reservation: {}",
                reservationId
        );

        // ---------------------------------------------------------
        // Find reservation
        // ---------------------------------------------------------

        InventoryReservation reservation =
                inventoryReservationRepository
                        .findById(reservationId)
                        .orElseThrow(() -> {

                            log.error(
                                    "Reservation not found: {}",
                                    reservationId
                            );

                            return new InventoryNotFoundException(
                                    "Reservation not found with id: "
                                            + reservationId
                            );
                        });

        // ---------------------------------------------------------
        // Validate status
        // ---------------------------------------------------------

        validateReservedStatus(
                reservation
        );

        LocalDateTime now =
                LocalDateTime.now();

        // ---------------------------------------------------------
        // Check expiration
        // ---------------------------------------------------------

        if (reservation.getExpiresAt() != null
                && reservation.getExpiresAt().isBefore(now)) {

            throw new IllegalStateException(
                    "Reservation has expired"
            );
        }

        // ---------------------------------------------------------
        // Lock inventory
        // ---------------------------------------------------------

        InventoryStock inventoryStock =
                inventoryStockRepository
                        .findByProductVariantIdAndLocationCodeForUpdate(
                                reservation.getProductVariantId(),
                                reservation.getLocationCode()
                        )
                        .orElseThrow(() -> {

                            log.error(
                                    "Inventory not found while confirming reservation: {}",
                                    reservationId
                            );

                            return new InventoryNotFoundException(
                                    "Inventory not found for product variant "
                                            + reservation.getProductVariantId()
                                            + " at location "
                                            + reservation.getLocationCode()
                            );
                        });

        // ---------------------------------------------------------
        // Validate reserved quantity
        // ---------------------------------------------------------

        if (inventoryStock.getQuantityReserved()
                < reservation.getQuantity()) {

            throw new InsufficientInventoryException(
                    "Reserved quantity is insufficient for confirmation"
            );
        }

        // ---------------------------------------------------------
        // Validate physical stock
        // ---------------------------------------------------------

        if (inventoryStock.getQuantityOnHand()
                < reservation.getQuantity()) {

            throw new InsufficientInventoryException(
                    "Quantity on hand is insufficient for confirmation"
            );
        }

        // ---------------------------------------------------------
        // Convert reservation to SALE
        //
        // quantity_on_hand decreases
        // quantity_reserved decreases
        // ---------------------------------------------------------

        inventoryStock.setQuantityOnHand(
                inventoryStock.getQuantityOnHand()
                        - reservation.getQuantity()
        );

        inventoryStock.setQuantityReserved(
                inventoryStock.getQuantityReserved()
                        - reservation.getQuantity()
        );

        inventoryStock.setStatus(
                determineStatus(
                        inventoryStock.getQuantityOnHand(),
                        inventoryStock.getReorderLevel()
                )
        );

        inventoryStock.setUpdatedAt(now);

        inventoryStockRepository.save(
                inventoryStock
        );

        // ---------------------------------------------------------
        // Mark reservation CONFIRMED
        // ---------------------------------------------------------

        reservation.setStatus(
                ReservationStatus.CONFIRMED
        );

        reservation.setUpdatedAt(now);

        InventoryReservation savedReservation =
                inventoryReservationRepository.save(
                        reservation
                );

        // ---------------------------------------------------------
        // Create SALE movement
        // ---------------------------------------------------------

        InventoryMovement movement =
                InventoryMovement.builder()
                        .movementId(UUID.randomUUID())
                        .productVariantId(
                                reservation.getProductVariantId()
                        )
                        .locationCode(
                                reservation.getLocationCode()
                        )
                        .movementType(
                                InventoryMovement.MovementType.SALE
                        )
                        .quantity(
                                reservation.getQuantity()
                        )
                        .referenceType(
                                "ORDER"
                        )
                        .referenceId(
                                reservation.getOrderId()
                        )
                        .reason(
                                "Reservation confirmed as sale"
                        )
                        .createdAt(
                                now
                        )
                        .build();

        inventoryMovementRepository.save(
                movement
        );

        log.info(
                "Reservation confirmed successfully: {}",
                reservationId
        );

        return toResponse(
                savedReservation
        );
    }

    // =========================================================
    // RELEASE RESERVATION
    // =========================================================

    @Override
    @Transactional
    public void releaseReservation(
            UUID reservationId) {

        log.info(
                "Releasing reservation: {}",
                reservationId
        );

        // ---------------------------------------------------------
        // Find reservation
        // ---------------------------------------------------------

        InventoryReservation reservation =
                inventoryReservationRepository
                        .findById(reservationId)
                        .orElseThrow(() -> {

                            log.error(
                                    "Reservation not found: {}",
                                    reservationId
                            );

                            return new InventoryNotFoundException(
                                    "Reservation not found with id: "
                                            + reservationId
                            );
                        });

        // ---------------------------------------------------------
        // Reservation must be RESERVED
        // ---------------------------------------------------------

        validateReservedStatus(
                reservation
        );

        // ---------------------------------------------------------
        // Lock inventory
        // ---------------------------------------------------------

        InventoryStock inventoryStock =
                inventoryStockRepository
                        .findByProductVariantIdAndLocationCodeForUpdate(
                                reservation.getProductVariantId(),
                                reservation.getLocationCode()
                        )
                        .orElseThrow(() -> {

                            log.error(
                                    "Inventory not found while releasing reservation: {}",
                                    reservationId
                            );

                            return new InventoryNotFoundException(
                                    "Inventory not found for product variant "
                                            + reservation.getProductVariantId()
                                            + " at location "
                                            + reservation.getLocationCode()
                            );
                        });

        // ---------------------------------------------------------
        // Validate reserved quantity
        // ---------------------------------------------------------

        if (inventoryStock.getQuantityReserved()
                < reservation.getQuantity()) {

            throw new InsufficientInventoryException(
                    "Reserved quantity is insufficient for release"
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        // ---------------------------------------------------------
        // Release reserved quantity
        // ---------------------------------------------------------

        inventoryStock.setQuantityReserved(
                inventoryStock.getQuantityReserved()
                        - reservation.getQuantity()
        );

        inventoryStock.setStatus(
                determineStatus(
                        inventoryStock.getQuantityOnHand(),
                        inventoryStock.getReorderLevel()
                )
        );

        inventoryStock.setUpdatedAt(now);

        inventoryStockRepository.save(
                inventoryStock
        );

        // ---------------------------------------------------------
        // Mark reservation RELEASED
        // ---------------------------------------------------------

        reservation.setStatus(
                ReservationStatus.RELEASED
        );

        reservation.setUpdatedAt(now);

        inventoryReservationRepository.save(
                reservation
        );

        // ---------------------------------------------------------
        // Create RELEASE movement
        // ---------------------------------------------------------

        InventoryMovement movement =
                InventoryMovement.builder()
                        .movementId(UUID.randomUUID())
                        .productVariantId(
                                reservation.getProductVariantId()
                        )
                        .locationCode(
                                reservation.getLocationCode()
                        )
                        .movementType(
                                InventoryMovement.MovementType.RELEASE
                        )
                        .quantity(
                                reservation.getQuantity()
                        )
                        .referenceType(
                                "ORDER"
                        )
                        .referenceId(
                                reservation.getOrderId()
                        )
                        .reason(
                                "Reservation released"
                        )
                        .createdAt(
                                now
                        )
                        .build();

        inventoryMovementRepository.save(
                movement
        );

        log.info(
                "Reservation released successfully: {}",
                reservationId
        );
    }

    // =========================================================
    // EXPIRE RESERVATIONS
    // =========================================================

    @Override
    @Transactional
    public List<ReservationResponseDTO> expireReservations() {

        log.info(
                "Starting reservation expiry process"
        );

        LocalDateTime now =
                LocalDateTime.now();

        // ---------------------------------------------------------
        // Find expired RESERVED reservations
        // ---------------------------------------------------------

        List<InventoryReservation> expiredCandidates =
                inventoryReservationRepository
                        .findByStatusAndExpiresAtBefore(
                                ReservationStatus.RESERVED,
                                now
                        );

        List<ReservationResponseDTO> expiredReservations =
                new ArrayList<>();

        // ---------------------------------------------------------
        // Process every expired reservation
        // ---------------------------------------------------------

        for (InventoryReservation reservation :
                expiredCandidates) {

            // -----------------------------------------------------
            // Lock inventory row
            // -----------------------------------------------------

            InventoryStock inventoryStock =
                    inventoryStockRepository
                            .findByProductVariantIdAndLocationCodeForUpdate(
                                    reservation.getProductVariantId(),
                                    reservation.getLocationCode()
                            )
                            .orElseThrow(() -> {

                                log.error(
                                        "Inventory not found while expiring reservation: {}",
                                        reservation.getReservationId()
                                );

                                return new InventoryNotFoundException(
                                        "Inventory not found for product variant "
                                                + reservation.getProductVariantId()
                                                + " at location "
                                                + reservation.getLocationCode()
                                );
                            });

            // -----------------------------------------------------
            // Validate reserved quantity
            // -----------------------------------------------------

            if (inventoryStock.getQuantityReserved()
                    < reservation.getQuantity()) {

                throw new InsufficientInventoryException(
                        "Reserved quantity is insufficient while expiring reservation "
                                + reservation.getReservationId()
                );
            }

            // -----------------------------------------------------
            // Release reserved quantity
            // -----------------------------------------------------

            inventoryStock.setQuantityReserved(
                    inventoryStock.getQuantityReserved()
                            - reservation.getQuantity()
            );

            inventoryStock.setStatus(
                    determineStatus(
                            inventoryStock.getQuantityOnHand(),
                            inventoryStock.getReorderLevel()
                    )
            );

            inventoryStock.setUpdatedAt(now);

            inventoryStockRepository.save(
                    inventoryStock
            );

            // -----------------------------------------------------
            // Mark reservation EXPIRED
            // -----------------------------------------------------

            reservation.setStatus(
                    ReservationStatus.EXPIRED
            );

            reservation.setUpdatedAt(now);

            InventoryReservation savedReservation =
                    inventoryReservationRepository.save(
                            reservation
                    );

            // -----------------------------------------------------
            // Create RELEASE movement
            // -----------------------------------------------------

            InventoryMovement movement =
                    InventoryMovement.builder()
                            .movementId(UUID.randomUUID())
                            .productVariantId(
                                    reservation.getProductVariantId()
                            )
                            .locationCode(
                                    reservation.getLocationCode()
                            )
                            .movementType(
                                    InventoryMovement.MovementType.RELEASE
                            )
                            .quantity(
                                    reservation.getQuantity()
                            )
                            .referenceType(
                                    "RESERVATION"
                            )
                            .referenceId(
                                    reservation
                                            .getReservationId()
                                            .toString()
                            )
                            .reason(
                                    "Reservation expired"
                            )
                            .createdAt(
                                    now
                            )
                            .build();

            inventoryMovementRepository.save(
                    movement
            );

            expiredReservations.add(
                    toResponse(
                            savedReservation
                    )
            );

            log.info(
                    "Reservation expired successfully: {}",
                    reservation.getReservationId()
            );
        }

        log.info(
                "Reservation expiry process completed. Count: {}",
                expiredReservations.size()
        );

        return expiredReservations;
    }

    // =========================================================
    // VALIDATE RESERVATION STATUS
    // =========================================================

    private void validateReservedStatus(
            InventoryReservation reservation) {

        if (reservation.getStatus()
                != ReservationStatus.RESERVED) {

            log.error(
                    "Invalid reservation state. ID: {}, status: {}",
                    reservation.getReservationId(),
                    reservation.getStatus()
            );

            throw new IllegalStateException(
                    "Reservation must be in RESERVED status. "
                            + "Current status: "
                            + reservation.getStatus()
            );
        }
    }

    // =========================================================
    // ENTITY -> RESPONSE DTO
    // =========================================================

    private ReservationResponseDTO toResponse(
            InventoryReservation reservation) {

        return ReservationResponseDTO.builder()
                .reservationId(
                        reservation.getReservationId()
                )
                .productVariantId(
                        reservation.getProductVariantId()
                )
                .locationCode(
                        reservation.getLocationCode()
                )
                .orderId(
                        reservation.getOrderId()
                )
                .quantity(
                        reservation.getQuantity()
                )
                .status(
                        reservation.getStatus()
                )
                .expiresAt(
                        reservation.getExpiresAt()
                )
                .createdAt(
                        reservation.getCreatedAt()
                )
                .updatedAt(
                        reservation.getUpdatedAt()
                )
                .build();
    }

    // =========================================================
    // DETERMINE STOCK STATUS
    // =========================================================

    private String determineStatus(
            Integer quantityOnHand,
            Integer reorderLevel) {

        if (quantityOnHand == null
                || quantityOnHand <= 0) {

            return "OUT_OF_STOCK";
        }

        int level =
                reorderLevel == null
                        ? 10
                        : reorderLevel;

        if (quantityOnHand <= level) {
            return "LOW_STOCK";
        }

        return "IN_STOCK";
    }
}