package com.booking.system.messaging;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BookingAutoCancelEvent implements Serializable {
    private Long bookingId;
    private String bookingNumber;
    private LocalDateTime createdAt;
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}