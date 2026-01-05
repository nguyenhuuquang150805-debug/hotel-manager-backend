package com.nguyenhuuquang.hotelmanagement.service;

import java.util.List;

import com.nguyenhuuquang.hotelmanagement.dto.RoomTypeDTO;

public interface RoomTypeService {
    RoomTypeDTO createRoomType(RoomTypeDTO roomTypeDTO);

    RoomTypeDTO updateRoomType(Long id, RoomTypeDTO roomTypeDTO);

    void deleteRoomType(Long id);

    RoomTypeDTO getRoomTypeById(Long id);

    List<RoomTypeDTO> getAllRoomTypes();
}