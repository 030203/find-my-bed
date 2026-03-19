package com.booking.system.messaging;

import com.booking.system.config.RabbitMqConfig;
import com.booking.system.entity.Booking;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class BookingMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public BookingMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
// 创建队列
    public void publishBookingCreated(Booking booking) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.BOOKING_EXCHANGE,
                RabbitMqConfig.BOOKING_NOTIFICATION_ROUTING_KEY,
                buildCreatedEvent(booking)
        );
    }
// 超时队列
    public void publishBookingAutoCancel(Booking booking) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.BOOKING_EXCHANGE,
                RabbitMqConfig.BOOKING_DELAY_ROUTING_KEY,
                buildAutoCancelEvent(booking)
        );
    }
// 取消队列
    public void publishBookingCancelled(Booking booking, boolean autoCancelled) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.BOOKING_EXCHANGE,
                RabbitMqConfig.BOOKING_CANCELLED_ROUTING_KEY,
                buildCancelledEvent(booking, autoCancelled)
        );
    }

    private BookingCreatedEvent buildCreatedEvent(Booking booking) {
        BookingCreatedEvent event = new BookingCreatedEvent();
        event.setBookingId(booking.getId());
        event.setBookingNumber(booking.getBookingNumber());
        event.setUserId(booking.getUser() != null ? booking.getUser().getId() : null);
        event.setMerchantId(booking.getProperty() != null ? booking.getProperty().getMerchantId() : null);
        event.setPropertyId(booking.getProperty() != null ? booking.getProperty().getId() : null);
        event.setPropertyName(booking.getProperty() != null ? booking.getProperty().getPropertyName() : null);
        event.setRoomTypeId(booking.getRoomType() != null ? booking.getRoomType().getId() : null);
        event.setRoomTypeName(booking.getRoomType() != null ? booking.getRoomType().getTypeName() : null);
        event.setContactName(booking.getContactName());
        event.setContactPhone(booking.getContactPhone());
        event.setContactEmail(booking.getContactEmail());
        event.setCheckInDate(booking.getCheckInDate());
        event.setCheckOutDate(booking.getCheckOutDate());
        event.setTotalAmount(booking.getTotalAmount());
        event.setCreatedAt(booking.getCreatedAt());
        return event;
    }

    private BookingAutoCancelEvent buildAutoCancelEvent(Booking booking) {
        BookingAutoCancelEvent event = new BookingAutoCancelEvent();
        event.setBookingId(booking.getId());
        event.setBookingNumber(booking.getBookingNumber());
        event.setCreatedAt(booking.getCreatedAt());
        return event;
    }

    private BookingCancelledEvent buildCancelledEvent(Booking booking, boolean autoCancelled) {
        BookingCancelledEvent event = new BookingCancelledEvent();
        event.setBookingId(booking.getId());
        event.setBookingNumber(booking.getBookingNumber());
        event.setUserId(booking.getUser() != null ? booking.getUser().getId() : null);
        event.setMerchantId(booking.getProperty() != null ? booking.getProperty().getMerchantId() : null);
        event.setPropertyName(booking.getProperty() != null ? booking.getProperty().getPropertyName() : null);
        event.setCancellationReason(booking.getCancellationReason());
        event.setAutoCancelled(autoCancelled);
        event.setCancellationTime(booking.getCancellationTime());
        return event;
    }
}
