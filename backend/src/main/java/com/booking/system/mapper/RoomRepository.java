package com.booking.system.mapper;

import com.booking.system.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByProperty_Id(Long propertyId);
    List<Room> findByRoomType_Id(Long roomTypeId);
    List<Room> findByStatus(Room.RoomStatus status);
    long countByRoomType_IdAndStatus(Long roomTypeId, Room.RoomStatus status);
    Optional<Room> findByIdAndRoomType_Id(Long id, Long roomTypeId);
}
