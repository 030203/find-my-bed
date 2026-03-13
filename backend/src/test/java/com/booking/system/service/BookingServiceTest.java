package com.booking.system.service;

import com.booking.system.entity.Booking;
import com.booking.system.mapper.BookingRepository;
import com.booking.system.mapper.RoomTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @InjectMocks
    private BookingService bookingService;

    private Booking paidBooking;

    @BeforeEach
    void setUp() {
        paidBooking = new Booking();
        paidBooking.setId(1L);
        paidBooking.setStatus(Booking.BookingStatus.PENDING);
        paidBooking.setPaymentStatus(Booking.PaymentStatus.PAID);
        paidBooking.setTotalAmount(BigDecimal.valueOf(100));
    }

    @Test
    void checkIn_shouldSetCheckedIn_whenPaidAndNotCancelled() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(paidBooking));
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Booking result = bookingService.checkIn(1L);

        assertEquals(Booking.BookingStatus.CHECKED_IN, result.getStatus());
        assertNotNull(result.getCheckInTime());

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        assertEquals(Booking.BookingStatus.CHECKED_IN, captor.getValue().getStatus());
    }

    @Test
    void checkIn_shouldRejectWhenUnpaid() {
        paidBooking.setPaymentStatus(Booking.PaymentStatus.UNPAID);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(paidBooking));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> bookingService.checkIn(1L));
        assertTrue(ex.getMessage().contains("has not been paid"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void checkOut_shouldSetCheckedOut_whenAlreadyCheckedIn() {
        paidBooking.setStatus(Booking.BookingStatus.CHECKED_IN);
        paidBooking.setPaymentStatus(Booking.PaymentStatus.PAID);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(paidBooking));
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Booking result = bookingService.checkOut(1L);

        assertEquals(Booking.BookingStatus.CHECKED_OUT, result.getStatus());
        assertNotNull(result.getCheckOutTime());
    }

    @Test
    void checkOut_shouldReject_whenNotCheckedIn() {
        paidBooking.setStatus(Booking.BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(paidBooking));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> bookingService.checkOut(1L));
        assertTrue(ex.getMessage().contains("must be checked in"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void checkOut_shouldReject_whenCancelledOrRefunded() {
        paidBooking.setStatus(Booking.BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(paidBooking));

        assertThrows(IllegalStateException.class, () -> bookingService.checkOut(1L));
        verify(bookingRepository, never()).save(any());
    }
}
