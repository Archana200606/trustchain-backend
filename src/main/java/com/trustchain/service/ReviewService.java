package com.trustchain.service;

import com.trustchain.dto.ReviewDTO;
import com.trustchain.model.Provider;
import com.trustchain.model.Review;
import com.trustchain.model.User;
import com.trustchain.repository.ProviderRepository;
import com.trustchain.repository.ReviewRepository;
import com.trustchain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service

public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final TrustScoreService trustScoreService;
    public ReviewService(ReviewRepository reviewRepository,
            ProviderRepository providerRepository,
            UserRepository userRepository,
            TrustScoreService trustScoreService) {
this.reviewRepository = reviewRepository;
this.providerRepository = providerRepository;
this.userRepository = userRepository;
this.trustScoreService = trustScoreService;
}
    public List<Review> getReviewsByProvider(Long providerId) {
        return reviewRepository.findByProviderIdOrderByCreatedAtDesc(providerId);
    }

    @Transactional
    public Review submitReview(ReviewDTO dto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Provider provider = providerRepository.findById(dto.getProviderId())
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        // Check if user already reviewed this provider
        reviewRepository.findByProviderIdAndUserId(dto.getProviderId(), user.getId())
                .ifPresent(r -> { throw new RuntimeException("You have already reviewed this provider"); });

        Review review = new Review();
        review.setProvider(provider);
        review.setUser(user);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setStatus(Review.ReviewStatus.ACTIVE);

        Review saved = reviewRepository.save(review);

        // Update provider review count and recalculate trust score
        provider.setTotalReviews(provider.getTotalReviews() + 1);
        providerRepository.save(provider);
        trustScoreService.calculateAndUpdateTrustScore(provider.getId());

        return saved;
    }

    @Transactional
    public Map<String, Object> upvoteReview(Long reviewId, String username) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setHelpfulVotes(review.getHelpfulVotes() + 1);
        reviewRepository.save(review);
        trustScoreService.calculateAndUpdateTrustScore(review.getProvider().getId());

        Map<String, Object> res = new HashMap<>();
        res.put("message", "Review upvoted successfully");
        res.put("helpfulVotes", review.getHelpfulVotes());
        return res;
    }

    @Transactional
    public Map<String, Object> reportReview(Long reviewId, String reason, String username) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setReportCount(review.getReportCount() + 1);
        if (review.getReportCount() >= 3) {
            review.setStatus(Review.ReviewStatus.FLAGGED);
        }
        reviewRepository.save(review);

        Map<String, Object> res = new HashMap<>();
        res.put("message", "Review reported successfully");
        res.put("reportCount", review.getReportCount());
        return res;
    }

    @Transactional
    public Review moderateReview(Long reviewId, String action) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        switch (action.toUpperCase()) {
            case "APPROVE" -> {
                review.setStatus(Review.ReviewStatus.ACTIVE);
                review.setVerified(true);
            }
            case "REMOVE" -> review.setStatus(Review.ReviewStatus.REMOVED);
            case "FLAG" -> review.setStatus(Review.ReviewStatus.FLAGGED);
        }
        Review saved = reviewRepository.save(review);
        trustScoreService.calculateAndUpdateTrustScore(review.getProvider().getId());
        return saved;
    }

    public List<Review> getFlaggedReviews() {
        return reviewRepository.findByReportCountGreaterThanOrderByReportCountDesc(0);
    }
}
