package com.trustchain.repository;

import com.trustchain.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProviderIdOrderByCreatedAtDesc(Long providerId);
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Review> findByStatus(Review.ReviewStatus status);
    Optional<Review> findByProviderIdAndUserId(Long providerId, Long userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.provider.id = :providerId AND r.status = 'ACTIVE'")
    Double findAverageRatingByProviderId(@Param("providerId") Long providerId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.provider.id = :providerId AND r.isVerified = true AND r.status = 'ACTIVE'")
    Long countVerifiedReviewsByProviderId(@Param("providerId") Long providerId);

    @Query("SELECT SUM(r.helpfulVotes) FROM Review r WHERE r.provider.id = :providerId AND r.status = 'ACTIVE'")
    Long sumHelpfulVotesByProviderId(@Param("providerId") Long providerId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.status = :status")
    Long countByStatus(@Param("status") Review.ReviewStatus status);

    List<Review> findByReportCountGreaterThanOrderByReportCountDesc(int threshold);
}
