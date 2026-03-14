package com.trustchain.controller;

import com.trustchain.model.Provider;
import com.trustchain.model.Review;
import com.trustchain.model.User;
import com.trustchain.repository.ProviderRepository;
import com.trustchain.repository.ReviewRepository;
import com.trustchain.repository.UserRepository;
import com.trustchain.service.ProviderService;
import com.trustchain.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")

@CrossOrigin(origins = "https://trustchain-frontend.onrender.com")
public class AdminController {

    private final ProviderService providerService;
    private final ReviewService reviewService;
    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;
    private final ReviewRepository reviewRepository;
    public AdminController(ProviderService providerService,
            ReviewService reviewService,
            UserRepository userRepository,
            ProviderRepository providerRepository,
            ReviewRepository reviewRepository) {

this.providerService = providerService;
this.reviewService = reviewService;
this.userRepository = userRepository;
this.providerRepository = providerRepository;
this.reviewRepository = reviewRepository;
}
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalProviders", providerRepository.count());
        stats.put("totalReviews", reviewRepository.count());
        stats.put("activeProviders", providerRepository.countByStatus(Provider.Status.ACTIVE));
        stats.put("flaggedReviews", reviewRepository.countByStatus(Review.ReviewStatus.FLAGGED));
        stats.put("pendingProviders", providerRepository.countByStatus(Provider.Status.PENDING));
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/providers")
    public ResponseEntity<List<Provider>> getAllProviders() {
        return ResponseEntity.ok(providerService.getAllProvidersForAdmin());
    }

    @PutMapping("/providers/{id}/status")
    public ResponseEntity<?> updateProviderStatus(@PathVariable Long id,
                                                   @RequestBody Map<String, String> body) {
        try {
            Provider provider = providerService.getProviderById(id);
            provider.setStatus(Provider.Status.valueOf(body.get("status").toUpperCase()));
            providerRepository.save(provider);
            return ResponseEntity.ok(Map.of("message", "Provider status updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/reviews/flagged")
    public ResponseEntity<List<Review>> getFlaggedReviews() {
        return ResponseEntity.ok(reviewService.getFlaggedReviews());
    }

    @PutMapping("/reviews/{id}/moderate")
    public ResponseEntity<?> moderateReview(@PathVariable Long id,
                                             @RequestBody Map<String, String> body) {
        try {
            Review review = reviewService.moderateReview(id, body.get("action"));
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

     @PutMapping("/users/{id}/status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            user.setActive(!user.isActive());
            userRepository.save(user);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User status updated");
            response.put("isActive", user.isActive());
            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build());
    }
}
