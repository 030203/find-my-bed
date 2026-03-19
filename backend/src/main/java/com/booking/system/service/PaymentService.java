package com.booking.system.service;

import com.booking.system.entity.Booking;
import com.booking.system.entity.Payment;
import com.booking.system.mapper.BookingRepository;
import com.booking.system.mapper.PaymentRepository;
import com.booking.system.messaging.PaymentMessagePublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentMessagePublisher paymentMessagePublisher;

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public Optional<Payment> getPaymentByNumber(String paymentNumber) {
        return paymentRepository.findByPaymentNumber(paymentNumber);
    }

    public List<Payment> getPaymentsByBookingId(Long bookingId) {
        return paymentRepository.findByBooking_Id(bookingId);
    }

    public List<Payment> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByBooking_User_Id(userId);
    }

    public List<Payment> getPaymentsByMerchantId(Long merchantId) {
        return paymentRepository.findByBooking_Property_MerchantId(merchantId);
    }

    public List<Payment> getPaymentsByStatus(Payment.PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    public List<Payment> getPayments(Long bookingId, Long userId, Long merchantId, Payment.PaymentStatus status) {
        List<Payment> payments;
        if (bookingId != null) {
            payments = paymentRepository.findByBooking_Id(bookingId);
        } else if (userId != null) {
            payments = paymentRepository.findByBooking_User_Id(userId);
        } else if (merchantId != null) {
            payments = paymentRepository.findByBooking_Property_MerchantId(merchantId);
        } else if (status != null) {
            payments = paymentRepository.findByStatus(status);
        } else {
            payments = paymentRepository.findAll();
        }

        if (status != null && payments != null) {
            payments = payments.stream()
                    .filter(p -> status.equals(p.getStatus()))
                    .collect(Collectors.toList());
        }
        return payments;
    }

    @Transactional
    public Payment createPayment(Payment payment) {
        if (payment.getPaymentNumber() == null) {
            payment.setPaymentNumber(generatePaymentNumber());
        }
        if (payment.getCreatedAt() == null) {
            payment.setCreatedAt(LocalDateTime.now());
        }
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment updatePayment(Long id, Payment payment) {
        Payment existing = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("支付记录不存在"));
        payment.setId(id);
        payment.setPaymentNumber(existing.getPaymentNumber());
        payment.setCreatedAt(existing.getCreatedAt());
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment processPayment(Long id, String transactionId) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("支付记录不存在"));
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setTransactionId(transactionId);
        payment.setPaidAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        Booking booking = payment.getBooking();
        if (booking != null) {
            booking.setPaymentStatus(Booking.PaymentStatus.PAID);
            if (booking.getStatus() == Booking.BookingStatus.PENDING) {
                booking.setStatus(Booking.BookingStatus.CONFIRMED);
            }
            booking.setPaidAmount(payment.getAmount());
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);
        }

        Payment saved = paymentRepository.save(payment);
        publishPaymentSucceededAfterCommit(saved);
        return saved;
    }

    @Transactional
    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }

    private String generatePaymentNumber() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void publishPaymentSucceededAfterCommit(Payment payment) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            paymentMessagePublisher.publishPaymentSucceeded(payment);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                paymentMessagePublisher.publishPaymentSucceeded(payment);
            }
        });
    }
}
