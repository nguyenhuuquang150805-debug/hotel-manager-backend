package com.nguyenhuuquang.hotelmanagement.controller;

import java.util.List;

import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nguyenhuuquang.hotelmanagement.assembler.RoomModelAssembler;
import com.nguyenhuuquang.hotelmanagement.dto.RoomDTO;
import com.nguyenhuuquang.hotelmanagement.entity.enums.RoomStatus;
import com.nguyenhuuquang.hotelmanagement.model.RoomModel;
import com.nguyenhuuquang.hotelmanagement.service.RoomService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoomController {

    private final RoomService roomService;
    private final RoomModelAssembler assembler;

    @PostMapping
    public ResponseEntity<RoomModel> createRoom(@Valid @RequestBody RoomDTO roomDTO) {
        RoomDTO created = roomService.createRoom(roomDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(created));
    }

    @GetMapping
    public ResponseEntity<CollectionModel<RoomModel>> getAllRooms() {
        List<RoomDTO> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(assembler.toCollectionModel(rooms));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomModel> getRoomById(@PathVariable Long id) {
        RoomDTO room = roomService.getRoomById(id);
        return ResponseEntity.ok(assembler.toModel(room));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<CollectionModel<RoomModel>> getRoomsByStatus(@PathVariable String status) {
        RoomStatus roomStatus = RoomStatus.valueOf(status.toUpperCase());
        List<RoomDTO> rooms = roomService.getRoomsByStatus(roomStatus);
        return ResponseEntity.ok(assembler.toCollectionModel(rooms));
    }

    @GetMapping("/floor/{floor}")
    public ResponseEntity<CollectionModel<RoomModel>> getRoomsByFloor(@PathVariable Integer floor) {
        List<RoomDTO> rooms = roomService.getRoomsByFloor(floor);
        return ResponseEntity.ok(assembler.toCollectionModel(rooms));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomModel> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomDTO roomDTO) {
        RoomDTO updated = roomService.updateRoom(id, roomDTO);
        return ResponseEntity.ok(assembler.toModel(updated));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateRoomStatus(@PathVariable Long id, @RequestParam String status) {
        RoomStatus roomStatus = RoomStatus.valueOf(status.toUpperCase());
        roomService.updateRoomStatus(id, roomStatus);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}