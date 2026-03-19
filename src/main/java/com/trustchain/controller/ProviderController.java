package com.trustchain.controller;

import com.trustchain.model.Provider;
import com.trustchain.service.ProviderService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/providers")

@CrossOrigin(origins = "https://trustchain-frontend.onrender.com")
public class ProviderController {

    private final ProviderService providerService;
    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }
    @GetMapping
    public ResponseEntity<List<Provider>> getAllProviders(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location) {
        if (category != null || location != null) {
            return ResponseEntity.ok(providerService.searchProviders(category, location));
        }
        return ResponseEntity.ok(providerService.getAllActiveProviders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProvider(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(providerService.getProviderById(id));
        } catch (RuntimeException e) {
            e.printStackTrace(); // 👈 VERY IMPORTANT

            return ResponseEntity
                    .status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping
    public ResponseEntity<?> addProvider(@RequestBody Provider provider, Authentication auth) {
        try {
            Provider saved = providerService.addProvider(provider, auth.getName());
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProvider(@PathVariable Long id, @RequestBody Provider provider, Authentication auth) {
        try {
            Provider updated = providerService.updateProvider(id, provider, auth.getName());
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<Provider.Category[]> getCategories() {
        return ResponseEntity.ok(Provider.Category.values());
    }
}
