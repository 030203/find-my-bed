package com.booking.system.messaging;

import com.booking.system.config.RabbitMqConfig;
import com.booking.system.service.BookingService;
import com.booking.system.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BookingMessageListener {

    private static final Logger log = LoggerFactory.getLogger(BookingMessageListener.class);

    private final BookingService bookingService;
    private final NotificationService notificationService;

    public BookingMessageListener(BookingService bookingService, NotificationService notificationService) {
        this.bookingService = bookingService;
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMqConfig.BOOKING_NOTIFICATION_QUEUE)
    public void handleBookingNotification(BookingCreatedEvent event) {
        notificationService.createBookingCreatedNotifications(event);
        log.info("Booking created event consumed, bookingId={}, bookingNumber={}",
                event.getBookingId(), event.getBookingNumber());
    }

    @RabbitListener(queues = RabbitMqConfig.BOOKING_AUTO_CANCEL_QUEUE)
    public void handleBookingAutoCancel(BookingAutoCancelEvent event) {
        bookingService.autoCancelUnpaidBooking(event.getBookingId());
    }

    @RabbitListener(queues = RabbitMqConfig.BOOKING_CANCELLED_QUEUE)
    public void handleBookingCancelled(BookingCancelledEvent event) {
        notificationService.createBookingCancelledNotifications(event);
        log.info("Booking cancelled event consumed, bookingId={}, bookingNumber={}, autoCancelled={}",
                event.getBookingId(), event.getBookingNumber(), event.getAutoCancelled());
    }

    @RabbitListener(queues = RabbitMqConfig.PAYMENT_SUCCESS_QUEUE)
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        notificationService.createPaymentSucceededNotifications(event);
        log.info("Payment succeeded event consumed, paymentId={}, bookingNumber={}",
                event.getPaymentId(), event.getBookingNumber());
    }
}
