package com.booking.system.messaging;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BookingCancelledEvent implements Serializable {
    private Long bookingId;
    private String bookingNumber;
    private Long userId;
    private Long merchantId;
    private String propertyName;
    private String cancellationReason;
    private Boolean autoCancelled;
    private LocalDateTime cancellationTime;

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getPropertyName() { return propertyName; }
    public void setPropertyName(String propertyName) { this.propertyName = propertyName; }
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    public Boolean getAutoCancelled() { return autoCancelled; }
    public void setAutoCancelled(Boolean autoCancelled) { this.autoCancelled = autoCancelled; }
    public LocalDateTime getCancellationTime() { return cancellationTime; }
    public void setCancellationTime(LocalDateTime cancellationTime) { this.cancellationTime = cancellationTime; }
}
