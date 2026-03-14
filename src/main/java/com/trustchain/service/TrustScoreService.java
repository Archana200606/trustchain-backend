package com.trustchain.service;

import com.trustchain.model.Provider;
import com.trustchain.repository.ProviderRepository;
import com.trustchain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service

public class TrustScoreService {

    private final ReviewRepository reviewRepository;
    private final ProviderRepository providerRepository;
    public TrustScoreService(ReviewRepository reviewRepository, ProviderRepository providerRepository) {
        this.reviewRepository = reviewRepository;
        this.providerRepository = providerRepository;
    }
    /**
     * Trust Score Formula:
     * Trust Score = (average_rating * 0.6) + (verified_reviews * 0.2) + (helpful_votes * 0.2)
     * Normalized to a scale of 0-5
     */
    @Transactional
    public double calculateAndUpdateTrustScore(Long providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        Double avgRating = reviewRepository.findAverageRatingByProviderId(providerId);
        Long verifiedReviews = reviewRepository.countVerifiedReviewsByProviderId(providerId);
        Long helpfulVotes = reviewRepository.sumHelpfulVotesByProviderId(providerId);

        if (avgRating == null) avgRating = 0.0;
        if (verifiedReviews == null) verifiedReviews = 0L;
        if (helpfulVotes == null) helpfulVotes = 0L;

        // Normalize verified_reviews: cap at 10 for score, map to 0-5 scale
        double normalizedVerified = Math.min(verifiedReviews / 2.0, 5.0);
        // Normalize helpful_votes: cap at 50, map to 0-5 scale
        double normalizedHelpful = Math.min(helpfulVotes / 10.0, 5.0);

        double trustScore = (avgRating * 0.6) + (normalizedVerified * 0.2) + (normalizedHelpful * 0.2);
        trustScore = Math.round(trustScore * 100.0) / 100.0; // round to 2 decimal places

        provider.setAverageRating(Math.round(avgRating * 100.0) / 100.0);
        provider.setVerifiedReviews(verifiedReviews.intValue());
        provider.setHelpfulVotes(helpfulVotes.intValue());
        provider.setTrustScore(trustScore);

        providerRepository.save(provider);
        return trustScore;
    }
}
