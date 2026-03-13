package com.booking.system.service;

import com.booking.system.entity.Property;
import com.booking.system.entity.RoomType;
import com.booking.system.mapper.PropertyRepository;
import com.booking.system.mapper.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RoomTypeService {
    
    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private PropertyRepository propertyRepository;
    
    public List<RoomType> getAllRoomTypes() {
        return roomTypeRepository.findAll();
    }
    
    public Optional<RoomType> getRoomTypeById(Long id) {
        return roomTypeRepository.findById(id);
    }
    
    public List<RoomType> getRoomTypesByPropertyId(Long propertyId) {
        return roomTypeRepository.findByProperty_Id(propertyId);
    }
    
    public List<RoomType> getActiveRoomTypesByPropertyId(Long propertyId) {
        return roomTypeRepository.findByProperty_IdAndStatus(propertyId, RoomType.RoomTypeStatus.ACTIVE);
    }
    
    @Transactional
    public RoomType createRoomType(RoomType roomType) {
        attachProperty(roomType);
        roomType.setCreatedAt(LocalDateTime.now());
        roomType.setUpdatedAt(LocalDateTime.now());
        return roomTypeRepository.save(roomType);
    }
    
    @Transactional
    public RoomType updateRoomType(Long id, RoomType roomType) {
        roomType.setId(id);
        attachProperty(roomType);
        roomType.setUpdatedAt(LocalDateTime.now());
        return roomTypeRepository.save(roomType);
    }
    
    @Transactional
    public void deleteRoomType(Long id) {
        roomTypeRepository.deleteById(id);
    }

    private void attachProperty(RoomType roomType) {
        Long pid = roomType.getPropertyId();
        if (pid == null && roomType.getProperty() != null) {
            pid = roomType.getProperty().getId();
        }
        if (pid == null) {
            throw new RuntimeException("民宿ID不能为空");
        }
        Property property = propertyRepository.findById(pid)
                .orElseThrow(() -> new RuntimeException("民宿不存在"));
        roomType.setProperty(property);
    }
}
