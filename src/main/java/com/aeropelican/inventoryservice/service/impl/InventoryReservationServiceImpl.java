package com.aeropelican.inventoryservice.service.impl;
import com.aeropelican.inventoryservice.dto.request.CreateReservationRequestDTO;
import com.aeropelican.inventoryservice.dto.response.InventoryReservationResponseDTO;
import com.aeropelican.inventoryservice.dto.response.ReservationExpirationResponseDTO;
import com.aeropelican.inventoryservice.entity.InventoryReservation;
import com.aeropelican.inventoryservice.entity.InventoryStock;
import com.aeropelican.inventoryservice.enums.MovementType;
import com.aeropelican.inventoryservice.enums.ReservationStatus;
import com.aeropelican.inventoryservice.repository.InventoryReservationRepository;
import com.aeropelican.inventoryservice.repository.InventoryStockRepository;
import com.aeropelican.inventoryservice.service.InventoryMovementService;
import com.aeropelican.inventoryservice.service.InventoryReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryReservationServiceImpl
        implements InventoryReservationService {

    private final InventoryReservationRepository reservationRepository;
    private final InventoryStockRepository stockRepository;
    private final InventoryMovementService movementService;
    // CREATE RESERVATION
    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {"inventoryById", "availability"},
            allEntries = true
    )
    public InventoryReservationResponseDTO reserve(
            CreateReservationRequestDTO request
    ) {

        log.info(
                "Creating reservation: variantId={}, orderId={}, quantity={}",
                request.getProductVariantId(),
                request.getOrderId(),
                request.getQuantity()
        );

        InventoryStock stock =
                stockRepository
                        .findByVariantAndLocationForUpdate(
                                request.getProductVariantId(),
                                request.getLocationCode()
                        )
                        .orElseThrow(() -> {

                            log.warn(
                                    "Inventory not found: variantId={}, location={}",
                                    request.getProductVariantId(),
                                    request.getLocationCode()
                            );

                            return new RuntimeException(
                                    "Inventory not found"
                            );
                        });

        int available =
                stock.getQuantityOnHand()
                        - stock.getQuantityReserved();
        //Check available quantity

        if (available < request.getQuantity()) {

            log.warn(
                    "Insufficient stock: variantId={}, available={}, requested={}",
                    request.getProductVariantId(),
                    available,
                    request.getQuantity()
            );

            throw new IllegalArgumentException(
                    "Insufficient inventory"
            );
        }
        // Increase reserved quantity
        stock.setQuantityReserved(
                stock.getQuantityReserved()
                        + request.getQuantity()
        );

        stockRepository.save(stock);
        // Create reservation
        InventoryReservation reservation =
                InventoryReservation.builder()
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
                                request.getExpiresAt()
                        )
                        .build();

        InventoryReservation saved =
                reservationRepository.save(reservation);
        //Create inventory movement
        movementService.createMovement(
                request.getProductVariantId(),
                request.getLocationCode(),
                MovementType.RESERVATION,
                request.getQuantity(),
                "RESERVATION",
                saved.getReservationId().toString(),
                "Inventory reserved"
        );

        log.info(
                "Reservation created successfully: reservationId={}",
                saved.getReservationId()
        );

        return toResponse(saved);
    }
    // GET RESERVATION BY ID
    @Override
    @Transactional(readOnly = true)
    public InventoryReservationResponseDTO getById(
            UUID reservationId
    ) {

        log.debug(
                "Fetching reservation: reservationId={}",
                reservationId
        );

        InventoryReservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Reservation not found: reservationId={}",
                                    reservationId
                            );

                            return new RuntimeException(
                                    "Reservation not found"
                            );
                        });

        return toResponse(reservation);
    }
    // CONFIRM RESERVATION
    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {"inventoryById", "availability"},
            allEntries = true
    )
    public InventoryReservationResponseDTO confirm(
            UUID reservationId
    ) {

        log.info(
                "Confirming reservation: reservationId={}",
                reservationId
        );

        /*
         * Lock reservation row
         */
        InventoryReservation reservation =
                reservationRepository.findByIdForUpdate(
                        reservationId
                ).orElseThrow(() -> {

                    log.warn(
                            "Reservation not found: reservationId={}",
                            reservationId
                    );

                    return new RuntimeException(
                            "Reservation not found"
                    );
                });

        /*
         * Only RESERVED reservations can be confirmed.
         */
        if (reservation.getStatus()
                != ReservationStatus.RESERVED) {

            log.warn(
                    "Cannot confirm reservation: reservationId={}, status={}",
                    reservationId,
                    reservation.getStatus()
            );

            throw new IllegalStateException(
                    "Only RESERVED reservations can be confirmed"
            );
        }

        /*
         * Update reservation status
         */
        reservation.setStatus(
                ReservationStatus.CONFIRMED
        );

        InventoryReservation saved =
                reservationRepository.save(reservation);

        /*
         * Lock inventory row
         */
        InventoryStock stock =
                stockRepository
                        .findByVariantAndLocationForUpdate(
                                reservation.getProductVariantId(),
                                reservation.getLocationCode()
                        )
                        .orElseThrow(() -> {

                            log.warn(
                                    "Inventory not found: variantId={}, location={}",
                                    reservation.getProductVariantId(),
                                    reservation.getLocationCode()
                            );

                            return new RuntimeException(
                                    "Inventory not found"
                            );
                        });

        /*
         * Remove confirmed quantity from physical stock
         */
        stock.setQuantityOnHand(
                stock.getQuantityOnHand()
                        - reservation.getQuantity()
        );

        /*
         * Remove quantity from reserved stock
         */
        stock.setQuantityReserved(
                stock.getQuantityReserved()
                        - reservation.getQuantity()
        );

        stockRepository.save(stock);

        /*
         * Create SALE movement
         */
        movementService.createMovement(
                reservation.getProductVariantId(),
                reservation.getLocationCode(),
                MovementType.SALE,
                reservation.getQuantity(),
                "RESERVATION",
                reservationId.toString(),
                "Reservation confirmed"
        );

        log.info(
                "Reservation confirmed successfully: reservationId={}",
                reservationId
        );

        return toResponse(saved);
    }


    // ============================================================
    // RELEASE RESERVATION
    // ============================================================

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {"inventoryById", "availability"},
            allEntries = true
    )
    public InventoryReservationResponseDTO release(
            UUID reservationId
    ) {

        log.info(
                "Releasing reservation: reservationId={}",
                reservationId
        );

        /*
         * Lock reservation row
         */
        InventoryReservation reservation =
                reservationRepository.findByIdForUpdate(
                        reservationId
                ).orElseThrow(() -> {

                    log.warn(
                            "Reservation not found: reservationId={}",
                            reservationId
                    );

                    return new RuntimeException(
                            "Reservation not found"
                    );
                });

        /*
         * Only RESERVED reservations can be released.
         */
        if (reservation.getStatus()
                != ReservationStatus.RESERVED) {

            log.warn(
                    "Cannot release reservation: reservationId={}, status={}",
                    reservationId,
                    reservation.getStatus()
            );

            throw new IllegalStateException(
                    "Only RESERVED reservations can be released"
            );
        }

        /*
         * Lock inventory row
         */
        InventoryStock stock =
                stockRepository
                        .findByVariantAndLocationForUpdate(
                                reservation.getProductVariantId(),
                                reservation.getLocationCode()
                        )
                        .orElseThrow(() -> {

                            log.warn(
                                    "Inventory not found: variantId={}, location={}",
                                    reservation.getProductVariantId(),
                                    reservation.getLocationCode()
                            );

                            return new RuntimeException(
                                    "Inventory not found"
                            );
                        });

        /*
         * Release reserved quantity
         */
        stock.setQuantityReserved(
                stock.getQuantityReserved()
                        - reservation.getQuantity()
        );

        stockRepository.save(stock);

        /*
         * Update reservation status
         */
        reservation.setStatus(
                ReservationStatus.RELEASED
        );

        InventoryReservation saved =
                reservationRepository.save(reservation);

        /*
         * Create RELEASE movement
         */
        movementService.createMovement(
                reservation.getProductVariantId(),
                reservation.getLocationCode(),
                MovementType.RELEASE,
                reservation.getQuantity(),
                "RESERVATION",
                reservationId.toString(),
                "Reservation released"
        );

        log.info(
                "Reservation released successfully: reservationId={}",
                reservationId
        );

        return toResponse(saved);
    }


    // ============================================================
    // EXPIRE RESERVATIONS
    // ============================================================

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {"inventoryById", "availability"},
            allEntries = true
    )
    public ReservationExpirationResponseDTO expireReservations() {

        log.info(
                "Starting reservation expiration job"
        );

        var reservations =
                reservationRepository
                        .findByStatusAndExpiresAtBefore(
                                ReservationStatus.RESERVED,
                                LocalDateTime.now()
                        );

        int count = 0;

        for (InventoryReservation reservation : reservations) {

            /*
             * Release reserved inventory.
             */
            release(
                    reservation.getReservationId()
            );

            /*
             * Change status from RELEASED to EXPIRED.
             */
            reservation.setStatus(
                    ReservationStatus.EXPIRED
            );

            reservationRepository.save(
                    reservation
            );

            count++;

            log.debug(
                    "Reservation expired: reservationId={}",
                    reservation.getReservationId()
            );
        }

        log.info(
                "Reservation expiration completed: count={}",
                count
        );

        return ReservationExpirationResponseDTO.builder()
                .expiredReservations(count)
                .message(
                        count + " reservations expired"
                )
                .build();
    }


    // ============================================================
    // ENTITY -> RESPONSE DTO
    // ============================================================

    private InventoryReservationResponseDTO toResponse(
            InventoryReservation reservation
    ) {

        return InventoryReservationResponseDTO.builder()
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
}