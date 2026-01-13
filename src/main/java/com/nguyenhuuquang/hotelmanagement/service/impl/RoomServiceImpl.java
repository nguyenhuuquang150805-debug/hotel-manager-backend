package com.nguyenhuuquang.hotelmanagement.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenhuuquang.hotelmanagement.dto.RoomDTO;
import com.nguyenhuuquang.hotelmanagement.entity.Room;
import com.nguyenhuuquang.hotelmanagement.entity.RoomType;
import com.nguyenhuuquang.hotelmanagement.entity.enums.LogType;
import com.nguyenhuuquang.hotelmanagement.entity.enums.RoomStatus;
import com.nguyenhuuquang.hotelmanagement.exception.ResourceNotFoundException;
import com.nguyenhuuquang.hotelmanagement.repository.RoomRepository;
import com.nguyenhuuquang.hotelmanagement.repository.RoomTypeRepository;
import com.nguyenhuuquang.hotelmanagement.service.RoomService;
import com.nguyenhuuquang.hotelmanagement.service.SystemLogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

        private final RoomRepository roomRepo;
        private final RoomTypeRepository roomTypeRepo;
        private final SystemLogService logService;

        @Override
        @Transactional
        public RoomDTO createRoom(RoomDTO roomDTO) {
                if (roomRepo.findByRoomNumber(roomDTO.getRoomNumber()).isPresent()) {
                        throw new IllegalArgumentException("Số phòng đã tồn tại");
                }

                RoomType roomType = roomTypeRepo.findByName(roomDTO.getRoomTypeName())
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng"));

                Room room = Room.builder()
                                .roomNumber(roomDTO.getRoomNumber())
                                .roomType(roomType)
                                .floor(roomDTO.getFloor())
                                .price(roomDTO.getPrice())
                                .status(RoomStatus.valueOf(roomDTO.getStatus()))
                                .description(roomDTO.getDescription())
                                .build();

                room = roomRepo.save(room);

                logService.log(LogType.SUCCESS, "Thêm phòng", "Admin",
                                String.format("Đã thêm phòng %s", room.getRoomNumber()));

                return convertToDTO(room);
        }

        @Override
        @Transactional
        public RoomDTO updateRoom(Long id, RoomDTO roomDTO) {
                Room room = roomRepo.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng"));

                RoomType roomType = roomTypeRepo.findByName(roomDTO.getRoomTypeName())
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng"));

                room.setRoomType(roomType);
                room.setFloor(roomDTO.getFloor());
                room.setPrice(roomDTO.getPrice());
                room.setStatus(RoomStatus.valueOf(roomDTO.getStatus()));
                room.setDescription(roomDTO.getDescription());

                room = roomRepo.save(room);

                logService.log(LogType.INFO, "Cập nhật phòng", "Admin",
                                String.format("Đã cập nhật thông tin phòng %s", room.getRoomNumber()));

                return convertToDTO(room);
        }

        @Override
        @Transactional
        public void deleteRoom(Long id) {
                Room room = roomRepo.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng"));

                if (room.getStatus() == RoomStatus.OCCUPIED || room.getStatus() == RoomStatus.RESERVED) {
                        throw new IllegalStateException("Không thể xóa phòng đang được thuê hoặc đã đặt");
                }

                String roomNumber = room.getRoomNumber();
                roomRepo.delete(room);

                logService.log(LogType.WARNING, "Xóa phòng", "Admin",
                                String.format("Đã xóa phòng %s", roomNumber));
        }

        @Override
        public RoomDTO getRoomById(Long id) {
                Room room = roomRepo.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng"));
                return convertToDTO(room);
        }

        @Override
        public List<RoomDTO> getAllRooms() {
                return roomRepo.findAll()
                                .stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        @Override
        public List<RoomDTO> getRoomsByStatus(RoomStatus status) {
                return roomRepo.findByStatus(status)
                                .stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        @Override
        public List<RoomDTO> getRoomsByFloor(Integer floor) {
                return roomRepo.findByFloor(floor)
                                .stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public void updateRoomStatus(Long id, RoomStatus status) {
                Room room = roomRepo.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng"));

                room.setStatus(status);
                roomRepo.save(room);

                logService.log(LogType.INFO, "Cập nhật trạng thái phòng", "Admin",
                                String.format("Đã cập nhật trạng thái phòng %s thành %s", room.getRoomNumber(),
                                                status));
        }

        private RoomDTO convertToDTO(Room room) {
                return RoomDTO.builder()
                                .id(room.getId())
                                .roomNumber(room.getRoomNumber())
                                .roomTypeName(room.getRoomType().getName())
                                .floor(room.getFloor())
                                .price(room.getPrice())
                                .status(room.getStatus().name())
                                .description(room.getDescription())
                                .build();
        }
}