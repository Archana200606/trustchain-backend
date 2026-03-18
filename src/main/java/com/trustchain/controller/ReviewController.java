package com.trustchain.controller;

import com.trustchain.dto.ReviewDTO;
import com.trustchain.model.Review;
import com.trustchain.service.ReviewService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "${cors.allowed-origins}")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<Review>> getReviewsByProvider(@PathVariable Long providerId) {
        return ResponseEntity.ok(reviewService.getReviewsByProvider(providerId));
    }

    @PostMapping
    public ResponseEntity<?> submitReview(@Valid @RequestBody ReviewDTO dto, Authentication auth) {
        try {
            Review review = reviewService.submitReview(dto, auth.getName());
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{reviewId}/upvote")
    public ResponseEntity<?> upvoteReview(@PathVariable Long reviewId, Authentication auth) {
        try {
            return ResponseEntity.ok(reviewService.upvoteReview(reviewId, auth.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{reviewId}/report")
    public ResponseEntity<?> reportReview(@PathVariable Long reviewId,
                                          @RequestBody Map<String, String> body,
                                          Authentication auth) {
        try {
            String reason = body.getOrDefault("reason", "Suspicious review");
            return ResponseEntity.ok(reviewService.reportReview(reviewId, reason, auth.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}