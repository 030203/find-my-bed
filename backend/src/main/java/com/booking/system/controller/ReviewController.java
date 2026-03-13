package com.booking.system.controller;

import com.booking.system.entity.Review;
import com.booking.system.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews(
            @RequestParam(required = false) Long propertyId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status) {
        if (propertyId != null) {
            return ResponseEntity.ok(reviewService.getReviewsByPropertyId(propertyId));
        }
        if (userId != null) {
            return ResponseEntity.ok(reviewService.getReviewsByUserId(userId));
        }
        if (status != null) {
            return ResponseEntity.ok(reviewService.getReviewsByStatus(
                    Review.ReviewStatus.valueOf(status)));
        }
        return ResponseEntity.ok(reviewService.getAllReviews());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        Optional<Review> review = reviewService.getReviewById(id);
        return review.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<Review> getReviewByBookingId(@PathVariable Long bookingId) {
        Optional<Review> review = reviewService.getReviewByBookingId(bookingId);
        return review.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody Review review) {
        return ResponseEntity.ok(reviewService.createReview(review));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable Long id, @RequestBody Review review) {
        return ResponseEntity.ok(reviewService.updateReview(id, review));
    }
    
    @PostMapping("/{id}/approve")
    public ResponseEntity<Review> approveReview(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.approveReview(id));
    }
    
    @PostMapping("/{id}/reply")
    public ResponseEntity<Review> replyReview(
            @PathVariable Long id,
            @RequestBody String replyContent) {
        return ResponseEntity.ok(reviewService.replyReview(id, replyContent));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok().build();
    }
}

