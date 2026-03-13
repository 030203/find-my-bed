package com.booking.system.service;

import com.booking.system.entity.Booking;
import com.booking.system.entity.RoomType;
import com.booking.system.mapper.BookingRepository;
import com.booking.system.mapper.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public Optional<Booking> getBookingByNumber(String bookingNumber) {
        return bookingRepository.findByBookingNumber(bookingNumber);
    }

    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUser_Id(userId);
    }

    public List<Booking> getBookingsByPropertyId(Long propertyId) {
        return bookingRepository.findByProperty_Id(propertyId);
    }

    public List<Booking> getBookingsByMerchantId(Long merchantId) {
        return bookingRepository.findByProperty_MerchantId(merchantId);
    }

    public List<Booking> getBookingsByStatus(String status) {
        return bookingRepository.findByStatus(Booking.BookingStatus.valueOf(status));
    }

    @Transactional
    public Booking createBooking(Booking booking) {
        applyDefaults(booking);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking updateBooking(Long id, Booking booking) {
        Booking existing = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setId(id);
        booking.setBookingNumber(existing.getBookingNumber());
        applyDefaults(booking);
        return bookingRepository.save(booking);
    }

    @Transactional
    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }

    @Transactional
    public Booking cancelBooking(Long id, String reason) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancellationReason(reason);
        booking.setCancellationTime(java.time.LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking checkIn(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED || booking.getStatus() == Booking.BookingStatus.REFUNDED) {
            throw new IllegalStateException("Cannot check in a cancelled or refunded booking");
        }
        Booking.PaymentStatus paymentStatus = booking.getPaymentStatus();
        if (paymentStatus == null || paymentStatus == Booking.PaymentStatus.UNPAID) {
            throw new IllegalStateException("Booking has not been paid");
        }
        if (booking.getStatus() == Booking.BookingStatus.CHECKED_IN) {
            return booking;
        }
        booking.setStatus(Booking.BookingStatus.CHECKED_IN);
        booking.setCheckInTime(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking checkOut(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED || booking.getStatus() == Booking.BookingStatus.REFUNDED) {
            throw new IllegalStateException("Cannot check out a cancelled or refunded booking");
        }
        Booking.PaymentStatus paymentStatus = booking.getPaymentStatus();
        if (paymentStatus == null || paymentStatus == Booking.PaymentStatus.UNPAID) {
            throw new IllegalStateException("Booking has not been paid");
        }
        if (booking.getStatus() != Booking.BookingStatus.CHECKED_IN) {
            throw new IllegalStateException("Booking must be checked in before checkout");
        }
        booking.setStatus(Booking.BookingStatus.CHECKED_OUT);
        booking.setCheckOutTime(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    private String generateBookingNumber() {
        return "BK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Fill derived fields so user-facing booking can be created with minimal payload.
     */
    private void applyDefaults(Booking booking) {
        if (booking.getBookingNumber() == null) {
            booking.setBookingNumber(generateBookingNumber());
        }

        if (booking.getCheckInDate() != null && booking.getCheckOutDate() != null) {
            long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
            booking.setNights((int) Math.max(nights, 1));
        }

        if (booking.getBasePrice() == null && booking.getRoomType() != null && booking.getRoomType().getId() != null) {
            Optional<RoomType> roomTypeOpt = roomTypeRepository.findById(booking.getRoomType().getId());
            roomTypeOpt.ifPresent(rt -> booking.setBasePrice(rt.getBasePrice()));
        }

        BigDecimal basePrice = booking.getBasePrice() != null ? booking.getBasePrice() : BigDecimal.ZERO;
        BigDecimal discount = booking.getDiscountAmount() != null ? booking.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal couponDiscount = booking.getCouponDiscount() != null ? booking.getCouponDiscount() : BigDecimal.ZERO;
        BigDecimal serviceFee = booking.getServiceFee() != null ? booking.getServiceFee() : BigDecimal.ZERO;
        int nights = booking.getNights() != null ? booking.getNights() : 1;
        BigDecimal total = basePrice.multiply(BigDecimal.valueOf(nights))
                .add(serviceFee)
                .subtract(discount)
                .subtract(couponDiscount);
        booking.setTotalAmount(total.max(BigDecimal.ZERO));

        if (booking.getStatus() == null) {
            booking.setStatus(Booking.BookingStatus.PENDING);
        }
        if (booking.getPaymentStatus() == null) {
            booking.setPaymentStatus(Booking.PaymentStatus.UNPAID);
        }
    }
}
