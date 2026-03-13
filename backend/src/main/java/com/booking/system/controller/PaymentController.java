package com.booking.system.controller;

import com.booking.system.entity.Payment;
import com.booking.system.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @GetMapping
    public ResponseEntity<?> getAllPayments(
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) String status) {
        Payment.PaymentStatus statusEnum = null;
        if (status != null) {
            try {
                statusEnum = Payment.PaymentStatus.valueOf(status);
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid status value: " + status));
            }
        }

        List<Payment> payments = paymentService.getPayments(bookingId, userId, merchantId, statusEnum);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        Optional<Payment> payment = paymentService.getPaymentById(id);
        return payment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/number/{paymentNumber}")
    public ResponseEntity<Payment> getPaymentByNumber(@PathVariable String paymentNumber) {
        Optional<Payment> payment = paymentService.getPaymentByNumber(paymentNumber);
        return payment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        return ResponseEntity.ok(paymentService.createPayment(payment));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Payment> updatePayment(@PathVariable Long id, @RequestBody Payment payment) {
        return ResponseEntity.ok(paymentService.updatePayment(id, payment));
    }
    
    @PostMapping("/{id}/process")
    public ResponseEntity<Payment> processPayment(
            @PathVariable Long id,
            @RequestParam String transactionId) {
        return ResponseEntity.ok(paymentService.processPayment(id, transactionId));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.ok().build();
    }
}
