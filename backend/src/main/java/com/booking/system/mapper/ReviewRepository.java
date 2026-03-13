package com.booking.system.mapper;

import com.booking.system.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByBooking_Id(Long bookingId);
    List<Review> findByProperty_Id(Long propertyId);
    List<Review> findByUser_Id(Long userId);
    List<Review> findByStatus(Review.ReviewStatus status);
}

