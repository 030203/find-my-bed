package com.booking.system.mapper;

import com.booking.system.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    Optional<Merchant> findByUser_Id(Long userId);
    List<Merchant> findByStatus(Merchant.MerchantStatus status);
    List<Merchant> findByCity(String city);
}

