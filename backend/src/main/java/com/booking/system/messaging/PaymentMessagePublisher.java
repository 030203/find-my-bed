package com.booking.system.messaging;

import com.booking.system.config.RabbitMqConfig;
import com.booking.system.entity.Booking;
import com.booking.system.entity.Payment;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public PaymentMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishPaymentSucceeded(Payment payment) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.BOOKING_EXCHANGE,
                RabbitMqConfig.PAYMENT_SUCCESS_ROUTING_KEY,
                buildPaymentSucceededEvent(payment)
        );
    }

    private PaymentSucceededEvent buildPaymentSucceededEvent(Payment payment) {
        PaymentSucceededEvent event = new PaymentSucceededEvent();
        Booking booking = payment.getBooking();
        event.setPaymentId(payment.getId());
        event.setPaymentNumber(payment.getPaymentNumber());
        event.setBookingId(booking != null ? booking.getId() : null);
        event.setBookingNumber(booking != null ? booking.getBookingNumber() : null);
        event.setUserId(booking != null && booking.getUser() != null ? booking.getUser().getId() : null);
        event.setMerchantId(booking != null && booking.getProperty() != null ? booking.getProperty().getMerchantId() : null);
        event.setPropertyName(booking != null && booking.getProperty() != null ? booking.getProperty().getPropertyName() : null);
        event.setAmount(payment.getAmount());
        event.setPaymentMethod(payment.getPaymentMethod());
        event.setPaidAt(payment.getPaidAt());
        return event;
    }
}
