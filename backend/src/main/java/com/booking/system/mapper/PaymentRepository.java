package com.booking.system.mapper;

import com.booking.system.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentNumber(String paymentNumber);
    List<Payment> findByBooking_Id(Long bookingId);
    List<Payment> findByBooking_User_Id(Long userId);
    List<Payment> findByBooking_Property_MerchantId(Long merchantId);
    List<Payment> findByStatus(Payment.PaymentStatus status);
}
