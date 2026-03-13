package com.booking.system.controller;

import com.booking.system.entity.Merchant;
import com.booking.system.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/merchants")
public class MerchantController {
    
    @Autowired
    private MerchantService merchantService;
    
    @GetMapping
    public ResponseEntity<List<Merchant>> getAllMerchants(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String city) {
        if (status != null) {
            return ResponseEntity.ok(merchantService.getMerchantsByStatus(
                    Merchant.MerchantStatus.valueOf(status)));
        }
        if (city != null) {
            return ResponseEntity.ok(merchantService.getMerchantsByCity(city));
        }
        return ResponseEntity.ok(merchantService.getAllMerchants());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Merchant> getMerchantById(@PathVariable Long id) {
        Optional<Merchant> merchant = merchantService.getMerchantById(id);
        return merchant.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Merchant> getMerchantByUserId(@PathVariable Long userId) {
        Optional<Merchant> merchant = merchantService.getMerchantByUserId(userId);
        return merchant.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Merchant> createMerchant(@RequestBody Merchant merchant) {
        return ResponseEntity.ok(merchantService.createMerchant(merchant));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Merchant> updateMerchant(@PathVariable Long id, @RequestBody Merchant merchant) {
        return ResponseEntity.ok(merchantService.updateMerchant(id, merchant));
    }
    
    @PostMapping("/{id}/approve")
    public ResponseEntity<Merchant> approveMerchant(
            @PathVariable Long id,
            @RequestParam Long auditBy,
            @RequestParam(required = false) String remark) {
        return ResponseEntity.ok(merchantService.approveMerchant(id, auditBy, remark));
    }
    
    @PostMapping("/{id}/reject")
    public ResponseEntity<Merchant> rejectMerchant(
            @PathVariable Long id,
            @RequestParam Long auditBy,
            @RequestParam(required = false) String remark) {
        return ResponseEntity.ok(merchantService.rejectMerchant(id, auditBy, remark));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMerchant(@PathVariable Long id) {
        merchantService.deleteMerchant(id);
        return ResponseEntity.ok().build();
    }
}

