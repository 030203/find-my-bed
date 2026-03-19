package com.booking.system.service;

import com.booking.system.entity.Merchant;
import com.booking.system.entity.Notification;
import com.booking.system.entity.User;
import com.booking.system.mapper.MerchantRepository;
import com.booking.system.mapper.NotificationRepository;
import com.booking.system.mapper.UserRepository;
import com.booking.system.messaging.BookingCancelledEvent;
import com.booking.system.messaging.BookingCreatedEvent;
import com.booking.system.messaging.PaymentSucceededEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    public List<Notification> getNotifications(Long userId, boolean unreadOnly) {
        if (userId == null) {
            return notificationRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        if (unreadOnly) {
            return notificationRepository.findByUser_IdAndIsReadFalseOrderByCreatedAtDesc(userId);
        }
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    public long countUnread(Long userId) {
        return notificationRepository.countByUser_IdAndIsReadFalse(userId);
    }

    @Transactional
    public Notification markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }
        return notification;
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUser_IdAndIsReadFalseOrderByCreatedAtDesc(userId);
        LocalDateTime readTime = LocalDateTime.now();
        notifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(readTime);
        });
        notificationRepository.saveAll(notifications);
        return notifications.size();
    }

    @Transactional
    public void createBookingCreatedNotifications(BookingCreatedEvent event) {
        saveNotification(
                event.getUserId(),
                Notification.NotificationType.BOOKING,
                "Booking created",
                String.format("Booking %s was created for %s. Please complete payment soon.",
                        event.getBookingNumber(), fallbackPropertyName(event.getPropertyName())),
                event.getBookingId(),
                event.getCreatedAt()
        );

        Long merchantUserId = resolveMerchantUserId(event.getMerchantId());
        if (!Objects.equals(merchantUserId, event.getUserId())) {
            saveNotification(
                    merchantUserId,
                    Notification.NotificationType.BOOKING,
                    "New booking received",
                    String.format("Property %s received booking %s. Please follow payment and check-in status.",
                            fallbackPropertyName(event.getPropertyName()), event.getBookingNumber()),
                    event.getBookingId(),
                    event.getCreatedAt()
            );
        }
    }

    @Transactional
    public void createPaymentSucceededNotifications(PaymentSucceededEvent event) {
        String amountText = event.getAmount() != null ? event.getAmount().toPlainString() : "0.00";
        saveNotification(
                event.getUserId(),
                Notification.NotificationType.PAYMENT,
                "Payment successful",
                String.format("Booking %s was paid successfully. Amount: %s.",
                        event.getBookingNumber(), amountText),
                event.getBookingId(),
                event.getPaidAt()
        );

        Long merchantUserId = resolveMerchantUserId(event.getMerchantId());
        if (!Objects.equals(merchantUserId, event.getUserId())) {
            saveNotification(
                    merchantUserId,
                    Notification.NotificationType.PAYMENT,
                    "Booking paid",
                    String.format("Booking %s for %s was paid. Amount: %s.",
                            event.getBookingNumber(), fallbackPropertyName(event.getPropertyName()), amountText),
                    event.getBookingId(),
                    event.getPaidAt()
            );
        }
    }

    @Transactional
    public void createBookingCancelledNotifications(BookingCancelledEvent event) {
        String userTitle = Boolean.TRUE.equals(event.getAutoCancelled()) ? "Booking auto-cancelled" : "Booking cancelled";
        String userContent = Boolean.TRUE.equals(event.getAutoCancelled())
                ? String.format("Booking %s was auto-cancelled because payment timed out.", event.getBookingNumber())
                : String.format("Booking %s was cancelled. %s", event.getBookingNumber(), normalizeReason(event.getCancellationReason()));
        saveNotification(
                event.getUserId(),
                Notification.NotificationType.BOOKING,
                userTitle,
                userContent,
                event.getBookingId(),
                event.getCancellationTime()
        );

        Long merchantUserId = resolveMerchantUserId(event.getMerchantId());
        if (!Objects.equals(merchantUserId, event.getUserId())) {
            String merchantContent = Boolean.TRUE.equals(event.getAutoCancelled())
                    ? String.format("Booking %s for %s was auto-cancelled because payment timed out.",
                            event.getBookingNumber(), fallbackPropertyName(event.getPropertyName()))
                    : String.format("Booking %s for %s was cancelled. %s",
                            event.getBookingNumber(), fallbackPropertyName(event.getPropertyName()), normalizeReason(event.getCancellationReason()));
            saveNotification(
                    merchantUserId,
                    Notification.NotificationType.BOOKING,
                    "Booking cancelled",
                    merchantContent,
                    event.getBookingId(),
                    event.getCancellationTime()
            );
        }
    }

    private void saveNotification(Long userId,
                                  Notification.NotificationType type,
                                  String title,
                                  String content,
                                  Long relatedId,
                                  LocalDateTime createdAt) {
        Notification notification = new Notification();
        if (userId != null) {
            Optional<User> user = userRepository.findById(userId);
            if (user.isEmpty()) {
                log.warn("Skip notification because user not found, userId={}, title={}", userId, title);
                return;
            }
            notification.setUser(user.get());
        }
        notification.setNotificationType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setIsRead(false);
        notification.setCreatedAt(createdAt != null ? createdAt : LocalDateTime.now());
        notificationRepository.save(notification);
    }

    private Long resolveMerchantUserId(Long merchantId) {
        if (merchantId == null) {
            return null;
        }
        return merchantRepository.findById(merchantId)
                .map(Merchant::getUser)
                .map(User::getId)
                .orElse(null);
    }

    private String fallbackPropertyName(String propertyName) {
        return StringUtils.hasText(propertyName) ? propertyName : "Unnamed property";
    }

    private String normalizeReason(String reason) {
        return StringUtils.hasText(reason) ? reason : "Create a new booking if needed.";
    }
}
