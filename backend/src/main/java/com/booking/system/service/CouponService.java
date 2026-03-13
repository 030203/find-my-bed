package com.booking.system.service;

import com.booking.system.entity.Coupon;
import com.booking.system.mapper.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    public List<Coupon> getCoupons(Long merchantId, String status) {
        if (merchantId != null) {
            return couponRepository.findByMerchant_Id(merchantId);
        }
        if (status != null) {
            try {
                return couponRepository.findByStatus(Coupon.CouponStatus.valueOf(status));
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Invalid status");
            }
        }
        return couponRepository.findAll();
    }

    public Optional<Coupon> getCouponById(Long id) {
        return couponRepository.findById(id);
    }

    @Transactional
    public Coupon createCoupon(Coupon coupon) {
        fillDefaults(coupon);
        return couponRepository.save(coupon);
    }

    @Transactional
    public Coupon updateCoupon(Long id, Coupon coupon) {
        coupon.setId(id);
        fillDefaults(coupon);
        return couponRepository.save(coupon);
    }

    @Transactional
    public void deleteCoupon(Long id) {
        couponRepository.deleteById(id);
    }

    private void fillDefaults(Coupon coupon) {
        if (coupon.getUsedQuantity() == null) {
            coupon.setUsedQuantity(0);
        }
        if (coupon.getCreatedAt() == null) {
            coupon.setCreatedAt(LocalDateTime.now());
        }
        if (coupon.getUpdatedAt() == null) {
            coupon.setUpdatedAt(LocalDateTime.now());
        }
    }
}
