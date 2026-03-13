package com.booking.system.service;

import com.booking.system.entity.Merchant;
import com.booking.system.mapper.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class MerchantService {
    
    @Autowired
    private MerchantRepository merchantRepository;
    
    public List<Merchant> getAllMerchants() {
        return merchantRepository.findAll();
    }
    
    public Optional<Merchant> getMerchantById(Long id) {
        return merchantRepository.findById(id);
    }
    
    public Optional<Merchant> getMerchantByUserId(Long userId) {
        return merchantRepository.findByUser_Id(userId);
    }
    
    public List<Merchant> getMerchantsByStatus(Merchant.MerchantStatus status) {
        return merchantRepository.findByStatus(status);
    }
    
    public List<Merchant> getMerchantsByCity(String city) {
        return merchantRepository.findByCity(city);
    }
    
    @Transactional
    public Merchant createMerchant(Merchant merchant) {
        return merchantRepository.save(merchant);
    }
    
    @Transactional
    public Merchant updateMerchant(Long id, Merchant merchant) {
        merchant.setId(id);
        return merchantRepository.save(merchant);
    }
    
    @Transactional
    public Merchant approveMerchant(Long id, Long auditBy, String remark) {
        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商户不存在"));
        merchant.setStatus(Merchant.MerchantStatus.APPROVED);
        merchant.setAuditBy(auditBy);
        merchant.setAuditRemark(remark);
        merchant.setAuditTime(java.time.LocalDateTime.now());
        return merchantRepository.save(merchant);
    }
    
    @Transactional
    public Merchant rejectMerchant(Long id, Long auditBy, String remark) {
        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商户不存在"));
        merchant.setStatus(Merchant.MerchantStatus.REJECTED);
        merchant.setAuditBy(auditBy);
        merchant.setAuditRemark(remark);
        merchant.setAuditTime(java.time.LocalDateTime.now());
        return merchantRepository.save(merchant);
    }
    
    @Transactional
    public void deleteMerchant(Long id) {
        merchantRepository.deleteById(id);
    }
}

