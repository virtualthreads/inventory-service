package com.aeropelican.inventoryservice.exception;

import com.aeropelican.inventoryservice.dto.response.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // INVENTORY NOT FOUND

    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<APIResponse<Void>> handleInventoryNotFound(
        InventoryNotFoundException ex
    ){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            APIResponse.<Void>builder()
            .success(false)
            .message(ex.getMessage())
            .build()
        );
    }

    // RESERVATION NOT FOUND

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<APIResponse<Void>> handleReservationNotFound(
        ReservationNotFoundException ex
    ){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            APIResponse.<Void>builder()
            .success(false)
            .message(ex.getMessage())
            .build()
        );
    }

    // INSUFFICIENT INVENTORY

    @ExceptionHandler(InsufficientInventoryException.class)
    public ResponseEntity<APIResponse<Void>> handleInsufficientInventory(
        InsufficientInventoryException ex
    ){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            APIResponse.<Void>builder()
            .success(false)
            .message(ex.getMessage())
            .build()
        );
    }

    // INVALID RESERVATION STATE

    @ExceptionHandler(InvalidReservationStateException.class)
    public ResponseEntity<APIResponse<Void>> handleInvalidReservationState(
        InvalidReservationStateException ex
    ){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            APIResponse.<Void>builder()
            .success(false)
            .message(ex.getMessage())
            .build()
        );
    }

    // VALIDATION ERRORS

    @ExceptionHandler(
        org.springframework.web.bind.MethodArgumentNotValidException.class
    )
    public ResponseEntity<APIResponse<Void>> handleValidationException(
        org.springframework.web.bind.MethodArgumentNotValidException ex
    ){
        String message = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("Invalid request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            APIResponse.<Void>builder()
            .success(false)
            .message(message)
            .build()
        );
    }

    // FALLBACK

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<Void>> handleGenericException(
        Exception ex
    ){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            APIResponse.<Void>builder()
            .success(false)
            .message("Internal server error")
            .build()
        );
    }
}