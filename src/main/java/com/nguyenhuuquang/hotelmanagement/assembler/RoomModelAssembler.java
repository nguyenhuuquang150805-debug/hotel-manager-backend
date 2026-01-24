package com.nguyenhuuquang.hotelmanagement.assembler;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.stereotype.Component;

import com.nguyenhuuquang.hotelmanagement.controller.RoomController;
import com.nguyenhuuquang.hotelmanagement.dto.RoomDTO;
import com.nguyenhuuquang.hotelmanagement.model.RoomModel;

@Component
public class RoomModelAssembler implements RepresentationModelAssembler<RoomDTO, RoomModel> {

    @Override
    public RoomModel toModel(RoomDTO dto) {
        RoomModel model = RoomModel.builder()
                .id(dto.getId())
                .roomNumber(dto.getRoomNumber())
                .roomTypeName(dto.getRoomTypeName())
                .floor(dto.getFloor())
                .price(dto.getPrice())
                .status(dto.getStatus())
                .description(dto.getDescription())
                .build();

        model.add(linkTo(methodOn(RoomController.class).getRoomById(dto.getId())).withSelfRel());

        model.add(linkTo(methodOn(RoomController.class).updateRoom(dto.getId(), null)).withRel("update"));

        if ("AVAILABLE".equals(dto.getStatus())) {
            model.add(linkTo(methodOn(RoomController.class).deleteRoom(dto.getId())).withRel("delete"));
        }

        model.add(linkTo(methodOn(RoomController.class).updateRoomStatus(dto.getId(), null)).withRel("updateStatus"));

        model.add(linkTo(methodOn(RoomController.class).getAllRooms()).withRel("rooms"));

        model.add(linkTo(methodOn(RoomController.class).getRoomsByStatus(dto.getStatus())).withRel("sameStatus"));

        model.add(linkTo(methodOn(RoomController.class).getRoomsByFloor(dto.getFloor())).withRel("sameFloor"));

        return model;
    }

    @Override
    public CollectionModel<RoomModel> toCollectionModel(Iterable<? extends RoomDTO> entities) {
        CollectionModel<RoomModel> roomModels = RepresentationModelAssembler.super.toCollectionModel(entities);

        roomModels.add(linkTo(methodOn(RoomController.class).getAllRooms()).withSelfRel());

        return roomModels;
    }
}