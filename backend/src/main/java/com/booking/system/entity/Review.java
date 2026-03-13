package com.booking.system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;
    
    @Column(name = "overall_rating", nullable = false)
    private Integer overallRating;
    
    @Column(name = "cleanliness_rating")
    private Integer cleanlinessRating;
    
    @Column(name = "service_rating")
    private Integer serviceRating;
    
    @Column(name = "location_rating")
    private Integer locationRating;
    
    @Column(name = "value_rating")
    private Integer valueRating;
    
    @Column(columnDefinition = "TEXT")
    private String comment;
    
    @Column(columnDefinition = "JSON")
    private String images;
    
    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;
    
    @Column(name = "is_verified")
    private Boolean isVerified = false;
    
    @Column(name = "helpful_count")
    private Integer helpfulCount = 0;
    
    @Column(name = "reply_content", columnDefinition = "TEXT")
    private String replyContent;
    
    @Column(name = "reply_time")
    private LocalDateTime replyTime;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReviewStatus status = ReviewStatus.PENDING;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    public enum ReviewStatus {
        PENDING, APPROVED, REJECTED, HIDDEN
    }
}

