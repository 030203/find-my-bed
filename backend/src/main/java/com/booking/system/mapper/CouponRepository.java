package com.booking.system.mapper;

import com.booking.system.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findByMerchant_Id(Long merchantId);
    List<Coupon> findByStatus(Coupon.CouponStatus status);
    Optional<Coupon> findByCouponName(String couponName);
}

