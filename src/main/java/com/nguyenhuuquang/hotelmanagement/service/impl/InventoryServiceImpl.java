package com.nguyenhuuquang.hotelmanagement.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenhuuquang.hotelmanagement.dto.InventoryDTO;
import com.nguyenhuuquang.hotelmanagement.entity.Inventory;
import com.nguyenhuuquang.hotelmanagement.entity.enums.LogType;
import com.nguyenhuuquang.hotelmanagement.exception.ResourceNotFoundException;
import com.nguyenhuuquang.hotelmanagement.repository.InventoryRepository;
import com.nguyenhuuquang.hotelmanagement.service.InventoryService;
import com.nguyenhuuquang.hotelmanagement.service.SystemLogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

        private final InventoryRepository inventoryRepo;
        private final SystemLogService logService;

        @Override
        @Transactional
        public InventoryDTO createInventory(InventoryDTO inventoryDTO) {
                Inventory inventory = Inventory.builder()
                                .name(inventoryDTO.getName())
                                .category(inventoryDTO.getCategory())
                                .quantity(inventoryDTO.getQuantity())
                                .unit(inventoryDTO.getUnit())
                                .minStock(inventoryDTO.getMinStock())
                                .price(inventoryDTO.getPrice())
                                .build();

                inventory = inventoryRepo.save(inventory);

                logService.log(LogType.SUCCESS, "Thêm hàng tồn kho", "Admin",
                                String.format("Đã thêm %s vào kho", inventory.getName()),
                                String.format("Số lượng: %d %s", inventory.getQuantity(), inventory.getUnit()));

                return convertToDTO(inventory);
        }

        @Override
        @Transactional
        public InventoryDTO updateInventory(Long id, InventoryDTO inventoryDTO) {
                Inventory inventory = inventoryRepo.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hàng tồn kho"));

                inventory.setName(inventoryDTO.getName());
                inventory.setCategory(inventoryDTO.getCategory());
                inventory.setQuantity(inventoryDTO.getQuantity());
                inventory.setUnit(inventoryDTO.getUnit());
                inventory.setMinStock(inventoryDTO.getMinStock());
                inventory.setPrice(inventoryDTO.getPrice());

                inventory = inventoryRepo.save(inventory);

                logService.log(LogType.INFO, "Cập nhật kho", "Admin",
                                String.format("Đã cập nhật %s", inventory.getName()),
                                String.format("Số lượng hiện tại: %d %s", inventory.getQuantity(),
                                                inventory.getUnit()));

                return convertToDTO(inventory);
        }

        @Override
        @Transactional
        public void deleteInventory(Long id) {
                Inventory inventory = inventoryRepo.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hàng tồn kho"));

                String name = inventory.getName();
                inventoryRepo.delete(inventory);

                logService.log(LogType.WARNING, "Xóa hàng tồn kho", "Admin",
                                String.format("Đã xóa %s khỏi kho", name));
        }

        @Override
        public InventoryDTO getInventoryById(Long id) {
                Inventory inventory = inventoryRepo.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hàng tồn kho"));
                return convertToDTO(inventory);
        }

        @Override
        public List<InventoryDTO> getAllInventory() {
                return inventoryRepo.findAll()
                                .stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        @Override
        public List<InventoryDTO> getInventoryByCategory(String category) {
                return inventoryRepo.findByCategory(category)
                                .stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        @Override
        public List<InventoryDTO> getLowStockItems() {
                List<Inventory> lowStockItems = inventoryRepo.findLowStockItems();

                if (!lowStockItems.isEmpty()) {
                        logService.log(LogType.WARNING, "Cảnh báo kho", "System",
                                        String.format("Có %d mặt hàng sắp hết", lowStockItems.size()));
                }

                return lowStockItems.stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public void updateStock(Long id, Integer quantity) {
                Inventory inventory = inventoryRepo.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hàng tồn kho"));

                int oldQuantity = inventory.getQuantity();
                inventory.setQuantity(quantity);
                inventoryRepo.save(inventory);

                String action = quantity > oldQuantity ? "Nhập hàng" : "Xuất hàng";
                int diff = Math.abs(quantity - oldQuantity);

                logService.log(LogType.INFO, action, "Admin",
                                String.format("%s: %s", action, inventory.getName()),
                                String.format("Số lượng thay đổi: %d %s (từ %d → %d)",
                                                diff, inventory.getUnit(), oldQuantity, quantity));
        }

        private InventoryDTO convertToDTO(Inventory inventory) {
                return InventoryDTO.builder()
                                .id(inventory.getId())
                                .name(inventory.getName())
                                .category(inventory.getCategory())
                                .quantity(inventory.getQuantity())
                                .unit(inventory.getUnit())
                                .minStock(inventory.getMinStock())
                                .price(inventory.getPrice())
                                .lastUpdated(inventory.getLastUpdated())
                                .build();
        }
}