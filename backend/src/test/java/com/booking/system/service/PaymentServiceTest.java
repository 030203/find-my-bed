package com.booking.system.service;

import com.booking.system.entity.Booking;
import com.booking.system.entity.Payment;
import com.booking.system.mapper.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void getPayments_shouldFilterWasteByStatusWhenMixed() {
        Payment success = payment(Payment.PaymentStatus.SUCCESS);
        Payment cancelled = payment(Payment.PaymentStatus.CANCELLED);
        Payment failed = payment(Payment.PaymentStatus.FAILED);
        when(paymentRepository.findAll()).thenReturn(List.of(success, cancelled, failed));

        List<Payment> result = paymentService.getPayments(null, null, null, Payment.PaymentStatus.SUCCESS);
        assertEquals(1, result.size());
        assertEquals(Payment.PaymentStatus.SUCCESS, result.get(0).getStatus());
    }

    private Payment payment(Payment.PaymentStatus status) {
        Payment p = new Payment();
        p.setStatus(status);
        Booking b = new Booking();
        p.setBooking(b);
        return p;
    }
}
