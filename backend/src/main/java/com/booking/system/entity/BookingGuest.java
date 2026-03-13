package com.booking.system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_guests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingGuest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
    
    @Column(name = "guest_name", nullable = false, length = 50)
    private String guestName;
    
    @Column(name = "id_card", length = 18)
    private String idCard;
    
    @Column(length = 20)
    private String phone;
    
    @Column(name = "is_main_guest")
    private Boolean isMainGuest = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}

