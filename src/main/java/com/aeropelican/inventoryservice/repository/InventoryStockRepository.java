package com.aeropelican.inventoryservice.repository;

import com.aeropelican.inventoryservice.dto.request.CreateInventoryStockRequest;
import com.aeropelican.inventoryservice.model.InventoryStock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class InventoryStockRepository {

    private final JdbcTemplate jdbcTemplate;

    public InventoryStockRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<InventoryStock> findAll() {

        String sql = """
                SELECT
                    inventory_id,
                    product_variant_id,
                    location_code,
                    quantity_on_hand,
                    quantity_reserved,
                    reorder_level,
                    status
                FROM inventory_stock
                ORDER BY product_variant_id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new InventoryStock(
                        rs.getString("inventory_id"),
                        rs.getLong("product_variant_id"),
                        rs.getString("location_code"),
                        rs.getInt("quantity_on_hand"),
                        rs.getInt("quantity_reserved"),
                        rs.getInt("reorder_level"),
                        rs.getString("status")
                )
        );
    }
    public List<InventoryStock> findByProductVariantId(Long variantId) {

        String sql = """
            SELECT
                inventory_id,
                product_variant_id,
                location_code,
                quantity_on_hand,
                quantity_reserved,
                reorder_level,
                status
            FROM inventory_stock
            WHERE product_variant_id = ?
            ORDER BY location_code
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        new InventoryStock(
                                rs.getString("inventory_id"),
                                rs.getLong("product_variant_id"),
                                rs.getString("location_code"),
                                rs.getInt("quantity_on_hand"),
                                rs.getInt("quantity_reserved"),
                                rs.getInt("reorder_level"),
                                rs.getString("status")
                        ),
                variantId
        );

    }
    public List<InventoryStock> findAllStock() {

        String sql = """
            SELECT
                inventory_id,
                product_variant_id,
                location_code,
                quantity_on_hand,
                quantity_reserved,
                reorder_level,
                status
            FROM inventory_stock
            ORDER BY product_variant_id, location_code
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new InventoryStock(
                        rs.getString("inventory_id"),
                        rs.getLong("product_variant_id"),
                        rs.getString("location_code"),
                        rs.getInt("quantity_on_hand"),
                        rs.getInt("quantity_reserved"),
                        rs.getInt("reorder_level"),
                        rs.getString("status")
                )
        );
    }
    public InventoryStock findByInventoryId(String inventoryId) {

        String sql = """
            SELECT
                inventory_id,
                product_variant_id,
                location_code,
                quantity_on_hand,
                quantity_reserved,
                reorder_level,
                status
            FROM inventory_stock
            WHERE inventory_id = ?
            """;

        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) ->
                        new InventoryStock(
                                rs.getString("inventory_id"),
                                rs.getLong("product_variant_id"),
                                rs.getString("location_code"),
                                rs.getInt("quantity_on_hand"),
                                rs.getInt("quantity_reserved"),
                                rs.getInt("reorder_level"),
                                rs.getString("status")
                        ),
                inventoryId
        );
    }
    public InventoryStock createStock(CreateInventoryStockRequest request) {

        String inventoryId = java.util.UUID.randomUUID().toString();

        String sql = """
            INSERT INTO inventory_stock
                (inventory_id, product_variant_id, location_code,
                 quantity_on_hand, quantity_reserved, reorder_level, status)
            VALUES (?, ?, ?, ?, 0, ?, 'IN_STOCK')
            """;

        jdbcTemplate.update(
                sql,
                inventoryId,
                request.getProductVariantId(),
                request.getLocationCode(),
                request.getQuantityOnHand(),
                request.getReorderLevel()
        );

        return findByInventoryId(inventoryId);
    }
}