# Inventory Service - MVP REST API Design

## 1. Service responsibility

Inventory Service owns:

- Stock quantity by product variant and location
- Reserved quantity
- Stock movements/audit history
- Temporary inventory reservations

It does NOT own:

- Product name, brand, price, SKU, category
- User details
- Orders

The Product Service remains the source of truth for product/variant metadata.
The Order Service remains the source of truth for order lifecycle.

The supplied Product Service currently uses BIGINT for `product_variants.variant_id`.
Therefore Inventory stores that value as `product_variant_id` without a database FK.
This is intentional microservice database isolation.

## 2. Recommended base URL

`/api/v1/inventory`

## 3. Stock APIs

### GET /api/v1/inventory/variants/{variantId}

Get aggregated inventory for a variant.

Response:

- productVariantId
- totalQuantityOnHand
- totalQuantityReserved
- totalAvailableQuantity
- locations[]

### GET /api/v1/inventory/variants/{variantId}/availability

Lightweight availability endpoint for Product/Cart/Order services.

Query parameter:

- `quantity` optional, default 1

Example:
`GET /api/v1/inventory/variants/3/availability?quantity=2`

Response:

```json
{
  "productVariantId": 3,
  "requestedQuantity": 2,
  "available": true,
  "availableQuantity": 35
}
```

### GET /api/v1/inventory/stock

Admin stock search.

Query parameters:

- variantId
- locationCode
- status
- page
- size

### GET /api/v1/inventory/stock/{inventoryId}

Get one stock record.

### POST /api/v1/inventory/stock

Create initial stock for a variant/location.

Request:

```json
{
  "productVariantId": 3,
  "locationCode": "BLR-WH01",
  "quantityOnHand": 100,
  "reorderLevel": 10
}
```

### PATCH /api/v1/inventory/stock/{inventoryId}

Update inventory configuration such as reorder level/status.
Do not allow clients to arbitrarily change reserved quantity.

### POST /api/v1/inventory/stock/{inventoryId}/adjust

Add/remove physical stock.

Request:

```json
{
  "quantity": 10,
  "movementType": "ADJUSTMENT_IN",
  "reason": "Supplier delivery"
}
```

For an outward adjustment:

```json
{
  "quantity": 3,
  "movementType": "ADJUSTMENT_OUT",
  "reason": "Damaged units"
}
```

## 4. Reservation APIs

Reservations are important because checkout can otherwise oversell stock.

### POST /api/v1/inventory/reservations

Reserve stock.

Request:

```json
{
  "productVariantId": 3,
  "locationCode": "BLR-WH01",
  "orderId": "ORD-10001",
  "quantity": 2,
  "reservationMinutes": 30
}
```

Response:

```json
{
  "reservationId": "uuid",
  "productVariantId": 3,
  "quantity": 2,
  "status": "RESERVED",
  "expiresAt": "2026-08-18T21:45:00Z"
}
```

### GET /api/v1/inventory/reservations/{reservationId}

Get reservation status.

### POST /api/v1/inventory/reservations/{reservationId}/confirm

Convert reserved stock into sold stock.

Expected transaction:

1. Validate reservation.
2. Reduce `quantity_on_hand`.
3. Reduce `quantity_reserved`.
4. Mark reservation `CONFIRMED`.
5. Insert `SALE` movement.

### POST /api/v1/inventory/reservations/{reservationId}/release

Release a reservation.

Expected transaction:

1. Validate reservation is `RESERVED`.
2. Reduce `quantity_reserved`.
3. Mark reservation `RELEASED`.
4. Insert `RELEASE` movement.

### POST /api/v1/inventory/reservations/expire

Internal/admin endpoint to expire old reservations.

Normally this should be triggered by a scheduled job rather than called by the frontend.

## 5. Movement APIs

### GET /api/v1/inventory/variants/{variantId}/movements

Return stock movement history.

Query parameters:

- locationCode
- movementType
- from
- to
- page
- size

This is primarily an admin/audit API.

## 6. API-to-service interaction

### Product Service

Product Service owns:
`product -> product_variant -> SKU/price/color/storage`

Inventory Service consumes:
`product_variant_id`

Recommended:

- Product creation does not automatically create stock unless the business process requires it.
- Admin creates inventory for a sellable variant.
- Product deletion should not physically delete inventory history.

### Cart Service

Cart Service should call:
`GET /api/v1/inventory/variants/{variantId}/availability?quantity=N`

Do not permanently reserve stock when an item is merely added to cart unless the business explicitly wants cart
reservations.

### Order Service

Recommended flow:

1. Customer places order.
2. Order Service requests reservation.
3. Inventory validates available stock.
4. Inventory increases reserved quantity.
5. Payment succeeds.
6. Order Service confirms reservation.
7. Inventory converts reservation to SALE.

If payment/order creation fails:
`POST /reservations/{reservationId}/release`

## 7. Important concurrency rule

The reservation operation must execute inside one database transaction.

Conceptually:

```sql
SELECT inventory_id, quantity_on_hand, quantity_reserved
FROM inventory_stock
WHERE product_variant_id = ?
  AND location_code = ?
FOR UPDATE;
```

Then validate:

`available = quantity_on_hand - quantity_reserved`

If:
`available < requestedQuantity`

return HTTP 409 Conflict.

Otherwise:
`quantity_reserved = quantity_reserved + requestedQuantity`

and insert a reservation + movement.

This row lock prevents two concurrent checkout requests from reserving the same stock.

## 8. Recommended HTTP status codes

| Situation                          | Status |
|------------------------------------|-------:|
| Successful GET                     |    200 |
| Stock/reservation created          |    201 |
| Successful PATCH/action            |    200 |
| Reservation released with no body  |    204 |
| Invalid request                    |    400 |
| Authentication missing             |    401 |
| Permission denied                  |    403 |
| Variant/inventory not found        |    404 |
| Insufficient stock / invalid state |    409 |
| Unexpected server error            |    500 |

## 9. Suggested Spring Boot package structure

```text
com.example.inventory
├── controller
│   ├── InventoryController
│   ├── ReservationController
│   └── InventoryMovementController
├── service
│   ├── InventoryService
│   └── ReservationService
├── repository
│   ├── InventoryStockRepository
│   ├── InventoryMovementRepository
│   └── InventoryReservationRepository
├── entity
│   ├── InventoryStock
│   ├── InventoryMovement
│   └── InventoryReservation
├── dto
│   ├── request
│   └── response
├── exception
├── mapper
└── config
```

## 10. MVP design decisions

1. No warehouse master table yet; use `location_code`.
2. No supplier table yet.
3. No batch/lot/serial-number tracking.
4. No inventory forecasting.
5. No separate available-quantity column; calculate:
   `quantity_on_hand - quantity_reserved`.
6. Keep movements append-only.
7. Use UUID for all Inventory-owned primary keys.
8. Do not create cross-service database foreign keys.
9. Use `@Transactional` for reserve, release, confirm and stock adjustment operations.
10. Add optimistic/pessimistic concurrency protection before production traffic.
