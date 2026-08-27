package com.aeropelican.inventoryservice.controller;

import com.aeropelican.inventoryservice.dto.request.CreateInventoryStockRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.aeropelican.inventoryservice.dto.response.InventoryAvailabilityResponse;
import com.aeropelican.inventoryservice.dto.response.InventoryVariantResponse;
import com.aeropelican.inventoryservice.model.InventoryStock;
import com.aeropelican.inventoryservice.service.InventoryStockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryStockController {

    private final InventoryStockService inventoryStockService;

    public InventoryStockController(InventoryStockService inventoryStockService) {
        this.inventoryStockService = inventoryStockService;
    }

    @GetMapping
    public List<InventoryStock> getAllInventory() {
        return inventoryStockService.getAllInventory();
    }

    @GetMapping("/variants/{variantId}")
    public InventoryVariantResponse getInventoryByVariantId(@PathVariable Long variantId) {
        return inventoryStockService.getInventoryVariantResponse(variantId);
    }
    @GetMapping("/variants/{variantId}/availability")
    public InventoryAvailabilityResponse getAvailability(
            @PathVariable Long variantId,
            @RequestParam(defaultValue = "1") int quantity) {

        return inventoryStockService.getAvailability(variantId, quantity);
    }
    @GetMapping("/stock")
    public List<InventoryStock> getStock() {
        return inventoryStockService.getAllStock();
    }
    @GetMapping("/stock/{inventoryId}")
    public InventoryStock getStockByInventoryId(@PathVariable String inventoryId) {
        return inventoryStockService.getStockByInventoryId(inventoryId);
    }
    @PostMapping("/stock")
    public InventoryStock createStock(@RequestBody CreateInventoryStockRequest request) {
        return inventoryStockService.createStock(request);
    }
}