package com.aeropelican.inventoryservice.exception;

public class InvalidReservationException
        extends RuntimeException {

    public InvalidReservationException(
            String message) {

        super(message);
    }
}