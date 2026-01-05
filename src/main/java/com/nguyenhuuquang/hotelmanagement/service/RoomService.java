package com.nguyenhuuquang.hotelmanagement.service;

import java.util.List;

import com.nguyenhuuquang.hotelmanagement.dto.RoomDTO;
import com.nguyenhuuquang.hotelmanagement.entity.enums.RoomStatus;

public interface RoomService {
    RoomDTO createRoom(RoomDTO roomDTO);

    RoomDTO updateRoom(Long id, RoomDTO roomDTO);

    void deleteRoom(Long id);

    RoomDTO getRoomById(Long id);

    List<RoomDTO> getAllRooms();

    List<RoomDTO> getRoomsByStatus(RoomStatus status);

    List<RoomDTO> getRoomsByFloor(Integer floor);

    void updateRoomStatus(Long id, RoomStatus status);
}