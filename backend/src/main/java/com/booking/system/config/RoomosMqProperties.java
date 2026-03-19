package com.booking.system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RefreshScope
@ConfigurationProperties(prefix = "roomos.mq")
public class RoomosMqProperties {

    private Duration bookingAutoCancelDelay = Duration.ofMinutes(15);

    public Duration getBookingAutoCancelDelay() {
        return bookingAutoCancelDelay;
    }

    public void setBookingAutoCancelDelay(Duration bookingAutoCancelDelay) {
        this.bookingAutoCancelDelay = bookingAutoCancelDelay;
    }
}