package com.booking.system.messaging;

import com.booking.system.entity.Payment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentSucceededEvent implements Serializable {
    private Long paymentId;
    private String paymentNumber;
    private Long bookingId;
    private String bookingNumber;
    private Long userId;
    private Long merchantId;
    private String propertyName;
    private BigDecimal amount;
    private Payment.PaymentMethod paymentMethod;
    private LocalDateTime paidAt;

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public String getPaymentNumber() { return paymentNumber; }
    public void setPaymentNumber(String paymentNumber) { this.paymentNumber = paymentNumber; }
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
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Payment.PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(Payment.PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}
