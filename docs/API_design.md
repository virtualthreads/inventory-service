## Main REST APIs

| Method | Endpoint                                                 | Purpose                    |
|--------|----------------------------------------------------------|----------------------------|
| GET    | `/api/v1/inventory/variants/{variantId}`                 | Get inventory              |
| GET    | `/api/v1/inventory/variants/{variantId}/availability`    | Check availability         |
| GET    | `/api/v1/inventory/stock`                                | Search inventory           |
| GET    | `/api/v1/inventory/stock/{inventoryId}`                  | Get stock record           |
| POST   | `/api/v1/inventory/stock`                                | Create stock               |
| PATCH  | `/api/v1/inventory/stock/{inventoryId}`                  | Update stock configuration |
| POST   | `/api/v1/inventory/stock/{inventoryId}/adjust`           | Add/remove physical stock  |
| POST   | `/api/v1/inventory/reservations`                         | Reserve stock              |
| GET    | `/api/v1/inventory/reservations/{reservationId}`         | Get reservation            |
| POST   | `/api/v1/inventory/reservations/{reservationId}/confirm` | Confirm reservation        |
| POST   | `/api/v1/inventory/reservations/{reservationId}/release` | Release reservation        |
| GET    | `/api/v1/inventory/variants/{variantId}/movements`       | Movement history           |

One particularly important implementation detail: reservation must use a database transaction with SELECT ... FOR UPDATE
so two concurrent orders cannot reserve the same units.

The complete API design, request/response examples, package structure, HTTP status codes, and service interaction flow
are in the inventory_service_api_design file under docs package.