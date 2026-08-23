package com.aeropelican.inventoryservice.service.Impl;

import com.aeropelican.inventoryservice.dto.request.ReserveInventoryRequest;
import com.aeropelican.inventoryservice.dto.response.InventoryReservationResponse;
import com.aeropelican.inventoryservice.entity.InventoryReservation;
import com.aeropelican.inventoryservice.entity.InventoryStock;
import com.aeropelican.inventoryservice.enums.ReservationStatus;
import com.aeropelican.inventoryservice.exception.InsufficientInventoryException;
import com.aeropelican.inventoryservice.exception.InvalidReservationException;
import com.aeropelican.inventoryservice.exception.InventoryNotFoundException;
import com.aeropelican.inventoryservice.exception.ReservationNotFoundException;
import com.aeropelican.inventoryservice.mapper.ReservationMapper;
import com.aeropelican.inventoryservice.repository.InventoryReservationRepository;
import com.aeropelican.inventoryservice.repository.InventoryStockRepository;
import com.aeropelican.inventoryservice.service.InventoryReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryReservationServiceImpl
        implements InventoryReservationService {

    private final InventoryStockRepository stockRepository;

    private final InventoryReservationRepository reservationRepository;

    private final ReservationMapper reservationMapper;


    // ============================================================
    // RESERVE
    // ============================================================

    @Override
    public InventoryReservationResponse reserve(
            ReserveInventoryRequest request) {

        InventoryStock stock =
                stockRepository
                        .findByProductVariantIdAndLocationCode(
                                request.getProductVariantId(),
                                request.getLocationCode()
                        )
                        .orElseThrow(
                                () -> new InventoryNotFoundException(
                                        request.getProductVariantId(),
                                        request.getLocationCode()
                                )
                        );


        if (stock.getAvailableQuantity()
                < request.getQuantity()) {

            throw new InsufficientInventoryException(
                    request.getProductVariantId(),
                    request.getLocationCode()
            );
        }


        stock.setReservedQuantity(
                stock.getReservedQuantity()
                        + request.getQuantity()
        );


        stock.setAvailableQuantity(
                stock.getAvailableQuantity()
                        - request.getQuantity()
        );


        stockRepository.save(stock);


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


        reservationRepository.save(reservation);


        return reservationMapper.toResponse(
                reservation
        );
    }


    // ============================================================
    // RELEASE
    // ============================================================

    @Override
    public InventoryReservationResponse release(
            UUID reservationId) {

        InventoryReservation reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(
                                () ->
                                        new ReservationNotFoundException(
                                                reservationId
                                        )
                        );


        if (reservation.getStatus()
                != ReservationStatus.RESERVED) {

            throw new InvalidReservationException(
                    "Reservation cannot be released. "
                            + "Current status: "
                            + reservation.getStatus()
            );
        }


        InventoryStock stock =
                stockRepository
                        .findByProductVariantIdAndLocationCode(
                                reservation.getProductVariantId(),
                                reservation.getLocationCode()
                        )
                        .orElseThrow(
                                () ->
                                        new InventoryNotFoundException(
                                                reservation
                                                        .getProductVariantId(),
                                                reservation
                                                        .getLocationCode()
                                        )
                        );


        stock.setReservedQuantity(
                stock.getReservedQuantity()
                        - reservation.getQuantity()
        );


        stock.setAvailableQuantity(
                stock.getAvailableQuantity()
                        + reservation.getQuantity()
        );


        stockRepository.save(stock);


        reservation.setStatus(
                ReservationStatus.RELEASED
        );


        reservationRepository.save(reservation);


        return reservationMapper.toResponse(
                reservation
        );
    }


    // ============================================================
    // COMPLETE
    // ============================================================

    @Override
    public InventoryReservationResponse complete(
            UUID reservationId) {

        InventoryReservation reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(
                                () ->
                                        new ReservationNotFoundException(
                                                reservationId
                                        )
                        );


        if (reservation.getStatus()
                != ReservationStatus.RESERVED) {

            throw new InvalidReservationException(
                    "Reservation cannot be completed. "
                            + "Current status: "
                            + reservation.getStatus()
            );
        }


        InventoryStock stock =
                stockRepository
                        .findByProductVariantIdAndLocationCode(
                                reservation.getProductVariantId(),
                                reservation.getLocationCode()
                        )
                        .orElseThrow(
                                () ->
                                        new InventoryNotFoundException(
                                                reservation
                                                        .getProductVariantId(),
                                                reservation
                                                        .getLocationCode()
                                        )
                        );


        stock.setReservedQuantity(
                stock.getReservedQuantity()
                        - reservation.getQuantity()
        );


        stock.setTotalQuantity(
                stock.getTotalQuantity()
                        - reservation.getQuantity()
        );


        stockRepository.save(stock);


        reservation.setStatus(
                ReservationStatus.CONFIRMED
        );


        reservationRepository.save(reservation);


        return reservationMapper.toResponse(
                reservation
        );
    }
}