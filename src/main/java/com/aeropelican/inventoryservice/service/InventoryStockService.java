package com.aeropelican.inventoryservice.service;

import com.aeropelican.inventoryservice.dto.request.CreateInventoryStockRequest;
import com.aeropelican.inventoryservice.dto.response.InventoryAvailabilityResponse;
import com.aeropelican.inventoryservice.dto.response.InventoryVariantResponse;
import com.aeropelican.inventoryservice.model.InventoryStock;
import com.aeropelican.inventoryservice.repository.InventoryStockRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryStockService {

    private final InventoryStockRepository inventoryStockRepository;

    public InventoryStockService(InventoryStockRepository inventoryStockRepository) {
        this.inventoryStockRepository = inventoryStockRepository;
    }

    public List<InventoryStock> getAllInventory() {
        return inventoryStockRepository.findAll();
    }
    public List<InventoryStock> getInventoryByVariantId(Long variantId) {
        return inventoryStockRepository.findByProductVariantId(variantId);
    }
    public InventoryVariantResponse getInventoryVariantResponse(Long variantId) {

        List<InventoryStock> stocks =
                inventoryStockRepository.findByProductVariantId(variantId);

        int totalOnHand = stocks.stream()
                .mapToInt(InventoryStock::getQuantityOnHand)
                .sum();

        int totalReserved = stocks.stream()
                .mapToInt(InventoryStock::getQuantityReserved)
                .sum();

        int totalAvailable = totalOnHand - totalReserved;

        return new InventoryVariantResponse(
                variantId,
                totalOnHand,
                totalReserved,
                totalAvailable,
                stocks
        );
    }
    public InventoryAvailabilityResponse getAvailability(Long variantId, int requestedQuantity) {

        List<InventoryStock> stocks =
                inventoryStockRepository.findByProductVariantId(variantId);

        int availableQuantity = stocks.stream()
                .mapToInt(stock ->
                        stock.getQuantityOnHand() - stock.getQuantityReserved())
                .sum();

        boolean available = availableQuantity >= requestedQuantity;

        return new InventoryAvailabilityResponse(
                variantId,
                requestedQuantity,
                available,
                availableQuantity
        );
    }
    public List<InventoryStock> getAllStock() {
        return inventoryStockRepository.findAllStock();
    }
    public InventoryStock getStockByInventoryId(String inventoryId) {
        return inventoryStockRepository.findByInventoryId(inventoryId);
    }
    public InventoryStock createStock(CreateInventoryStockRequest request) {
        return inventoryStockRepository.createStock(request);
    }
}