package com.booking.system.controller;

import com.booking.system.entity.RoomType;
import com.booking.system.service.RoomTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/room-types")
public class RoomTypeController {
    
    @Autowired
    private RoomTypeService roomTypeService;
    
    @GetMapping
    public ResponseEntity<List<RoomType>> getAllRoomTypes(
            @RequestParam(required = false) Long propertyId) {
        if (propertyId != null) {
            return ResponseEntity.ok(roomTypeService.getRoomTypesByPropertyId(propertyId));
        }
        return ResponseEntity.ok(roomTypeService.getAllRoomTypes());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RoomType> getRoomTypeById(@PathVariable Long id) {
        Optional<RoomType> roomType = roomTypeService.getRoomTypeById(id);
        return roomType.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/property/{propertyId}/active")
    public ResponseEntity<List<RoomType>> getActiveRoomTypesByPropertyId(@PathVariable Long propertyId) {
        return ResponseEntity.ok(roomTypeService.getActiveRoomTypesByPropertyId(propertyId));
    }
    
    @PostMapping
    public ResponseEntity<RoomType> createRoomType(@RequestBody RoomType roomType) {
        return ResponseEntity.ok(roomTypeService.createRoomType(roomType));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<RoomType> updateRoomType(@PathVariable Long id, @RequestBody RoomType roomType) {
        return ResponseEntity.ok(roomTypeService.updateRoomType(id, roomType));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoomType(@PathVariable Long id) {
        roomTypeService.deleteRoomType(id);
        return ResponseEntity.ok().build();
    }
}

