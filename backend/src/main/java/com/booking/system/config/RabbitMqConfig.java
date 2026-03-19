package com.booking.system.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String BOOKING_EXCHANGE = "roomos.booking.exchange";
    public static final String BOOKING_NOTIFICATION_QUEUE = "roomos.booking.notification.queue";
    public static final String BOOKING_NOTIFICATION_ROUTING_KEY = "booking.notification.created";
    public static final String BOOKING_DELAY_QUEUE = "roomos.booking.auto-cancel.delay.queue";
    public static final String BOOKING_DELAY_ROUTING_KEY = "booking.auto-cancel.delay";
    public static final String BOOKING_AUTO_CANCEL_QUEUE = "roomos.booking.auto-cancel.queue";
    public static final String BOOKING_AUTO_CANCEL_ROUTING_KEY = "booking.auto-cancel.release";
    public static final String BOOKING_CANCELLED_QUEUE = "roomos.booking.cancelled.queue";
    public static final String BOOKING_CANCELLED_ROUTING_KEY = "booking.cancelled";
    public static final String PAYMENT_SUCCESS_QUEUE = "roomos.payment.success.queue";
    public static final String PAYMENT_SUCCESS_ROUTING_KEY = "payment.success";

    @Bean
    public DirectExchange bookingExchange() {
        return new DirectExchange(BOOKING_EXCHANGE, true, false);
    }

    @Bean
    public Queue bookingNotificationQueue() {
        return QueueBuilder.durable(BOOKING_NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Binding bookingNotificationBinding() {
        return BindingBuilder.bind(bookingNotificationQueue())
                .to(bookingExchange())
                .with(BOOKING_NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public Queue bookingDelayQueue(RoomosMqProperties mqProperties) {
        return QueueBuilder.durable(BOOKING_DELAY_QUEUE)
                .ttl((int) mqProperties.getBookingAutoCancelDelay().toMillis())
                .deadLetterExchange(BOOKING_EXCHANGE)
                .deadLetterRoutingKey(BOOKING_AUTO_CANCEL_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding bookingDelayBinding(RoomosMqProperties mqProperties) {
        return BindingBuilder.bind(bookingDelayQueue(mqProperties))
                .to(bookingExchange())
                .with(BOOKING_DELAY_ROUTING_KEY);
    }

    @Bean
    public Queue bookingAutoCancelQueue() {
        return QueueBuilder.durable(BOOKING_AUTO_CANCEL_QUEUE).build();
    }

    @Bean
    public Binding bookingAutoCancelBinding() {
        return BindingBuilder.bind(bookingAutoCancelQueue())
                .to(bookingExchange())
                .with(BOOKING_AUTO_CANCEL_ROUTING_KEY);
    }

    @Bean
    public Queue bookingCancelledQueue() {
        return QueueBuilder.durable(BOOKING_CANCELLED_QUEUE).build();
    }

    @Bean
    public Binding bookingCancelledBinding() {
        return BindingBuilder.bind(bookingCancelledQueue())
                .to(bookingExchange())
                .with(BOOKING_CANCELLED_ROUTING_KEY);
    }

    @Bean
    public Queue paymentSuccessQueue() {
        return QueueBuilder.durable(PAYMENT_SUCCESS_QUEUE).build();
    }

    @Bean
    public Binding paymentSuccessBinding() {
        return BindingBuilder.bind(paymentSuccessQueue())
                .to(bookingExchange())
                .with(PAYMENT_SUCCESS_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
