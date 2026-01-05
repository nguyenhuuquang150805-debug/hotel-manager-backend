package com.nguyenhuuquang.hotelmanagement.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenhuuquang.hotelmanagement.dto.RoomTypeDTO;
import com.nguyenhuuquang.hotelmanagement.entity.RoomType;
import com.nguyenhuuquang.hotelmanagement.entity.enums.LogType;
import com.nguyenhuuquang.hotelmanagement.exception.ResourceNotFoundException;
import com.nguyenhuuquang.hotelmanagement.repository.RoomTypeRepository;
import com.nguyenhuuquang.hotelmanagement.service.RoomTypeService;
import com.nguyenhuuquang.hotelmanagement.service.SystemLogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepo;
    private final SystemLogService logService;

    @Override
    @Transactional
    public RoomTypeDTO createRoomType(RoomTypeDTO roomTypeDTO) {
        if (roomTypeRepo.findByName(roomTypeDTO.getName()).isPresent()) {
            throw new IllegalArgumentException("Tên loại phòng đã tồn tại");
        }

        RoomType roomType = RoomType.builder()
                .name(roomTypeDTO.getName())
                .description(roomTypeDTO.getDescription())
                .basePrice(roomTypeDTO.getBasePrice())
                .maxOccupancy(roomTypeDTO.getMaxOccupancy())
                .amenities(roomTypeDTO.getAmenities())
                .build();

        roomType = roomTypeRepo.save(roomType);

        logService.log(LogType.SUCCESS, "Thêm loại phòng", "Admin",
                String.format("Đã thêm loại phòng %s", roomType.getName()));

        return convertToDTO(roomType);
    }

    @Override
    @Transactional
    public RoomTypeDTO updateRoomType(Long id, RoomTypeDTO roomTypeDTO) {
        RoomType roomType = roomTypeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng"));

        roomType.setName(roomTypeDTO.getName());
        roomType.setDescription(roomTypeDTO.getDescription());
        roomType.setBasePrice(roomTypeDTO.getBasePrice());
        roomType.setMaxOccupancy(roomTypeDTO.getMaxOccupancy());
        roomType.setAmenities(roomTypeDTO.getAmenities());

        roomType = roomTypeRepo.save(roomType);

        logService.log(LogType.INFO, "Cập nhật loại phòng", "Admin",
                String.format("Đã cập nhật loại phòng %s", roomType.getName()));

        return convertToDTO(roomType);
    }

    @Override
    @Transactional
    public void deleteRoomType(Long id) {
        RoomType roomType = roomTypeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng"));

        if (roomType.getRooms() != null && !roomType.getRooms().isEmpty()) {
            throw new IllegalStateException("Không thể xóa loại phòng đang có phòng sử dụng");
        }

        String name = roomType.getName();
        roomTypeRepo.delete(roomType);

        logService.log(LogType.WARNING, "Xóa loại phòng", "Admin",
                String.format("Đã xóa loại phòng %s", name));
    }

    @Override
    public RoomTypeDTO getRoomTypeById(Long id) {
        RoomType roomType = roomTypeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng"));
        return convertToDTO(roomType);
    }

    @Override
    public List<RoomTypeDTO> getAllRoomTypes() {
        return roomTypeRepo.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private RoomTypeDTO convertToDTO(RoomType roomType) {
        return RoomTypeDTO.builder()
                .id(roomType.getId())
                .name(roomType.getName())
                .description(roomType.getDescription())
                .basePrice(roomType.getBasePrice())
                .maxOccupancy(roomType.getMaxOccupancy())
                .amenities(roomType.getAmenities())
                .build();
    }
}