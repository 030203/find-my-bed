package com.booking.system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCoupon {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserCouponStatus status = UserCouponStatus.UNUSED;
    
    @Column(name = "used_at")
    private LocalDateTime usedAt;
    
    @Column(name = "used_booking_id")
    private Long usedBookingId;
    
    @Column(name = "obtained_at")
    private LocalDateTime obtainedAt = LocalDateTime.now();
    
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;
    
    public enum UserCouponStatus {
        UNUSED, USED, EXPIRED
    }
}

