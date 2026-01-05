package com.nguyenhuuquang.hotelmanagement.service;

import java.util.List;

import com.nguyenhuuquang.hotelmanagement.dto.InventoryDTO;

public interface InventoryService {
    InventoryDTO createInventory(InventoryDTO inventoryDTO);

    InventoryDTO updateInventory(Long id, InventoryDTO inventoryDTO);

    void deleteInventory(Long id);

    InventoryDTO getInventoryById(Long id);

    List<InventoryDTO> getAllInventory();

    List<InventoryDTO> getInventoryByCategory(String category);

    List<InventoryDTO> getLowStockItems();

    void updateStock(Long id, Integer quantity);
}