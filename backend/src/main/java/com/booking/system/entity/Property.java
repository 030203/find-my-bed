package com.booking.system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "properties")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Property {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;
    
    @Column(name = "property_name", nullable = false, length = 100)
    private String propertyName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", nullable = false, length = 20)
    private PropertyType propertyType = PropertyType.HOMESTAY;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, length = 255)
    private String address;
    
    @Column(nullable = false, length = 50)
    private String province;
    
    @Column(nullable = false, length = 50)
    private String city;
    
    @Column(length = 50)
    private String district;
    
    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;
    
    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;
    
    @Column(name = "check_in_time")
    private LocalTime checkInTime = LocalTime.of(14, 0);
    
    @Column(name = "check_out_time")
    private LocalTime checkOutTime = LocalTime.of(12, 0);
    
    @Column(name = "total_rooms")
    private Integer totalRooms = 0;
    
    @Column(name = "available_rooms")
    private Integer availableRooms = 0;
    
    @Column(precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;
    
    @Column(name = "total_reviews")
    private Integer totalReviews = 0;
    
    @Column(name = "price_range_min", precision = 10, scale = 2)
    private BigDecimal priceRangeMin;
    
    @Column(name = "price_range_max", precision = 10, scale = 2)
    private BigDecimal priceRangeMax;
    
    @Column(columnDefinition = "JSON")
    private String facilities;
    
    @Column(columnDefinition = "JSON")
    private String images;
    
    @Column(columnDefinition = "JSON")
    private String policies;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PropertyStatus status = PropertyStatus.DRAFT;
    
    @Column(name = "is_featured")
    private Boolean isFeatured = false;
    
    @Column(name = "view_count")
    private Integer viewCount = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    public enum PropertyType {
        HOMESTAY, HOTEL, APARTMENT, VILLA, OTHER
    }
    
    public enum PropertyStatus {
        DRAFT, PENDING, APPROVED, REJECTED, SUSPENDED
    }
}
