package com.booking.system.controller;

import com.booking.system.entity.Booking;
import com.booking.system.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long propertyId,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) String status) {
        if (userId != null) {
            return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
        }
        if (propertyId != null) {
            return ResponseEntity.ok(bookingService.getBookingsByPropertyId(propertyId));
        }
        if (merchantId != null) {
            return ResponseEntity.ok(bookingService.getBookingsByMerchantId(merchantId));
        }
        if (StringUtils.hasText(status)) {
            try {
                return ResponseEntity.ok(bookingService.getBookingsByStatus(status));
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        Optional<Booking> booking = bookingService.getBookingById(id);
        return booking.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{bookingNumber}")
    public ResponseEntity<Booking> getBookingByNumber(@PathVariable String bookingNumber) {
        Optional<Booking> booking = bookingService.getBookingByNumber(bookingNumber);
        return booking.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Booking booking) {
        try {
            return ResponseEntity.ok(bookingService.createBooking(booking));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable Long id, @RequestBody Booking booking) {
        try {
            return ResponseEntity.ok(bookingService.updateBooking(id, booking));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(@PathVariable Long id, @RequestBody String reason) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, reason));
    }

    @PostMapping("/{id}/check-in")
    public ResponseEntity<?> checkIn(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(bookingService.checkIn(id));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/check-out")
    public ResponseEntity<?> checkOut(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(bookingService.checkOut(id));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
