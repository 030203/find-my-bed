package com.booking.system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Transient
    private Long propertyId;
    
    @Column(name = "type_name", nullable = false, length = 50)
    private String typeName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "max_occupancy", nullable = false)
    private Integer maxOccupancy = 2;
    
    @Column(name = "bed_type", length = 50)
    private String bedType;
    
    @Column(name = "room_size", precision = 6, scale = 2)
    private BigDecimal roomSize;
    
    @Column(columnDefinition = "JSON")
    private String amenities;
    
    @Column(columnDefinition = "JSON")
    private String images;
    
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RoomTypeStatus status = RoomTypeStatus.ACTIVE;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getPropertyId() {
        if (propertyId != null) {
            return propertyId;
        }
        return property != null ? property.getId() : null;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
        if (propertyId != null) {
            Property p = new Property();
            p.setId(propertyId);
            this.property = p;
        }
    }
    
    public enum RoomTypeStatus {
        ACTIVE, INACTIVE
    }
}
