package com.booking.system.mapper;

import com.booking.system.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    // 通过Property的ID查询
    List<RoomType> findByProperty_Id(Long propertyId);
    List<RoomType> findByProperty_IdAndStatus(Long propertyId, RoomType.RoomTypeStatus status);
}

