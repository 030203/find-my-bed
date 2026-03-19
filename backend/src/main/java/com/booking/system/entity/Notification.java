package com.booking.system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 20)
    private NotificationType notificationType = NotificationType.SYSTEM;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "related_id")
    private Long relatedId;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum NotificationType {
        SYSTEM, BOOKING, PAYMENT, REVIEW, PROMOTION
    }
}
