package com.booking.system.controller.dto;

import com.booking.system.entity.Property;

public class TopPropertyResponse {
    private Property property;
    private Long bookingCount;

    public TopPropertyResponse() {
    }

    public TopPropertyResponse(Property property, Long bookingCount) {
        this.property = property;
        this.bookingCount = bookingCount;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public Long getBookingCount() {
        return bookingCount;
    }

    public void setBookingCount(Long bookingCount) {
        this.bookingCount = bookingCount;
    }
}
