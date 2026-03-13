package com.booking.system.service;

import com.booking.system.entity.Review;
import com.booking.system.mapper.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }
    
    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }
    
    public Optional<Review> getReviewByBookingId(Long bookingId) {
        return reviewRepository.findByBooking_Id(bookingId);
    }
    
    public List<Review> getReviewsByPropertyId(Long propertyId) {
        return reviewRepository.findByProperty_Id(propertyId);
    }
    
    public List<Review> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUser_Id(userId);
    }
    
    public List<Review> getReviewsByStatus(Review.ReviewStatus status) {
        return reviewRepository.findByStatus(status);
    }
    
    @Transactional
    public Review createReview(Review review) {
        // 检查是否已经评价过
        Optional<Review> existing = reviewRepository.findByBooking_Id(review.getBooking().getId());
        if (existing.isPresent()) {
            throw new RuntimeException("该预定已经评价过了");
        }
        review.setStatus(Review.ReviewStatus.PENDING);
        return reviewRepository.save(review);
    }
    
    @Transactional
    public Review updateReview(Long id, Review review) {
        review.setId(id);
        return reviewRepository.save(review);
    }
    
    @Transactional
    public Review approveReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("评价不存在"));
        review.setStatus(Review.ReviewStatus.APPROVED);
        return reviewRepository.save(review);
    }
    
    @Transactional
    public Review replyReview(Long id, String replyContent) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("评价不存在"));
        review.setReplyContent(replyContent);
        review.setReplyTime(java.time.LocalDateTime.now());
        return reviewRepository.save(review);
    }
    
    @Transactional
    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }
}

