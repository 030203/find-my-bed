package com.booking.system.controller.dto;

import com.booking.system.entity.Property;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 聚合民宿详情数据，避免前端二次请求和实体循环引用。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDetailResponse {
    private Property property;
    private List<SimpleRoomType> roomTypes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleRoomType {
        private Long id;
        private String typeName;
        private String description;
        private Integer maxOccupancy;
        private String bedType;
        private BigDecimal roomSize;
        private String amenities;
        private String images;
        private BigDecimal basePrice;
        private String status;
        private Integer sortOrder;
        private LocalDateTime createdAt;
    }
}
