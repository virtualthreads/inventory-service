-- ================================================================
-- INVENTORY SERVICE - DDL
-- MySQL 8.x
-- Database primary keys are UUIDs stored as CHAR(36).
--
-- Product Service currently exposes product_variants.variant_id as
-- BIGINT. Therefore product_variant_id below is an EXTERNAL reference,
-- not a local FK. This preserves microservice database ownership.
-- ================================================================

CREATE
DATABASE IF NOT EXISTS ecom_inventory;
USE
ecom_inventory;

DROP TABLE IF EXISTS inventory_reservations;
DROP TABLE IF EXISTS inventory_movements;
DROP TABLE IF EXISTS inventory_stock;

-- ----------------------------------------------------------------
-- 1. INVENTORY STOCK
-- One row represents stock for a product variant at one stock location.
-- A location is deliberately a simple code for the MVP; no warehouse
-- master table is required yet.
-- ----------------------------------------------------------------
CREATE TABLE inventory_stock
(
    inventory_id       CHAR(36)    NOT NULL,
    product_variant_id BIGINT      NOT NULL,
    location_code      VARCHAR(30) NOT NULL,
    quantity_on_hand   INT         NOT NULL DEFAULT 0,
    quantity_reserved  INT         NOT NULL DEFAULT 0,
    reorder_level      INT         NOT NULL DEFAULT 10,
    status             ENUM('IN_STOCK','LOW_STOCK','OUT_OF_STOCK','INACTIVE')
                       NOT NULL DEFAULT 'OUT_OF_STOCK',
    created_at         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (inventory_id),
    UNIQUE KEY uq_inventory_variant_location
        (product_variant_id, location_code),
    CONSTRAINT chk_inventory_on_hand CHECK (quantity_on_hand >= 0),
    CONSTRAINT chk_inventory_reserved CHECK
        (quantity_reserved >= 0 AND quantity_reserved <= quantity_on_hand),
    CONSTRAINT chk_inventory_reorder CHECK (reorder_level >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_inventory_variant
    ON inventory_stock (product_variant_id);
CREATE INDEX idx_inventory_status
    ON inventory_stock (status);

-- ----------------------------------------------------------------
-- 2. INVENTORY MOVEMENTS
-- Immutable audit trail for stock changes.
-- Examples: RECEIPT, ADJUSTMENT, RESERVATION, RELEASE, SALE, RETURN.
-- ----------------------------------------------------------------
CREATE TABLE inventory_movements
(
    movement_id        CHAR(36)    NOT NULL,
    product_variant_id BIGINT      NOT NULL,
    location_code      VARCHAR(30) NOT NULL,
    movement_type      ENUM(
        'RECEIPT',
        'ADJUSTMENT_IN',
        'ADJUSTMENT_OUT',
        'RESERVATION',
        'RELEASE',
        'SALE',
        'RETURN'
    ) NOT NULL,
    quantity           INT         NOT NULL,
    reference_type     VARCHAR(30) NULL,
    reference_id       VARCHAR(100) NULL,
    reason             VARCHAR(255) NULL,
    created_at         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (movement_id),
    CONSTRAINT chk_movement_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_movement_variant
    ON inventory_movements (product_variant_id);
CREATE INDEX idx_movement_reference
    ON inventory_movements (reference_type, reference_id);
CREATE INDEX idx_movement_created
    ON inventory_movements (created_at);

-- ----------------------------------------------------------------
-- 3. INVENTORY RESERVATIONS
-- Temporary stock holds made during checkout/order creation.
-- ----------------------------------------------------------------
CREATE TABLE inventory_reservations
(
    reservation_id     CHAR(36)     NOT NULL,
    product_variant_id BIGINT       NOT NULL,
    location_code      VARCHAR(30)  NOT NULL,
    order_id           VARCHAR(100) NOT NULL,
    quantity           INT          NOT NULL,
    status             ENUM('RESERVED','RELEASED','CONFIRMED','EXPIRED')
                       NOT NULL DEFAULT 'RESERVED',
    expires_at         TIMESTAMP NULL,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (reservation_id),
    CONSTRAINT chk_reservation_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_reservation_order
    ON inventory_reservations (order_id);
CREATE INDEX idx_reservation_variant_status
    ON inventory_reservations (product_variant_id, status);
CREATE INDEX idx_reservation_expiry
    ON inventory_reservations (status, expires_at);
