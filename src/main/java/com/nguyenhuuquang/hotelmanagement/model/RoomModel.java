package com.nguyenhuuquang.hotelmanagement.model;

import java.math.BigDecimal;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "rooms", itemRelation = "room")
public class RoomModel extends RepresentationModel<RoomModel> {
    private Long id;
    private String roomNumber;
    private String roomTypeName;
    private Integer floor;
    private BigDecimal price;
    private String status;
    private String description;
}