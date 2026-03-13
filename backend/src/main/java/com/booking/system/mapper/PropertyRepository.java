package com.booking.system.mapper;

import com.booking.system.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByMerchantId(Long merchantId);
    List<Property> findByCity(String city);
    List<Property> findByStatus(Property.PropertyStatus status);
    List<Property> findByIsFeatured(Boolean isFeatured);
}
