package com.trustchain.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class Review {
	public Long getId() { return id; } public void setId(Long id) { this.id = id; } public Provider getProvider() { return provider; } public void setProvider(Provider provider) { this.provider = provider; } public User getUser() { return user; } public void setUser(User user) { this.user = user; } public Integer getRating() { return rating; } public void setRating(Integer rating) { this.rating = rating; } public String getComment() { return comment; } public void setComment(String comment) { this.comment = comment; } public Integer getHelpfulVotes() { return helpfulVotes; } public void setHelpfulVotes(Integer helpfulVotes) { this.helpfulVotes = helpfulVotes; } public boolean isVerified() { return isVerified; } public void setVerified(boolean isVerified) { this.isVerified = isVerified; } public ReviewStatus getStatus() { return status; } public void setStatus(ReviewStatus status) { this.status = status; } public Integer getReportCount() { return reportCount; } public void setReportCount(Integer reportCount) { this.reportCount = reportCount; } public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; } public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 FIX 1: Prevent lazy + recursion crash
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    @JsonIgnore
    private Provider provider;

    // 🔥 FIX 2: Prevent lazy crash for user also
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String comment;

    @Column(name = "helpful_votes")
    private Integer helpfulVotes = 0;

    @Column(name = "is_verified")
    private boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    private ReviewStatus status = ReviewStatus.ACTIVE;

    @Column(name = "report_count")
    private Integer reportCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ReviewStatus {
        ACTIVE, FLAGGED, REMOVED
    }

	
    // getters & setters (same as yours)
}