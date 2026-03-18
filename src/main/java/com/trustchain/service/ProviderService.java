package com.trustchain.service;

import com.trustchain.model.Provider;
import com.trustchain.model.User;
import com.trustchain.repository.ProviderRepository;
import com.trustchain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProviderService {

    private final ProviderRepository providerRepository;
    private final UserRepository userRepository;

    public ProviderService(ProviderRepository providerRepository, UserRepository userRepository) {
        this.providerRepository = providerRepository;
        this.userRepository = userRepository;
    }
    public List<Provider> getAllActiveProviders() {
        return providerRepository.findByStatusOrderByTrustScoreDesc(Provider.Status.ACTIVE);
    }
    @Transactional(readOnly = true)
    public Provider getProviderById(Long id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found with id: " + id));

        return provider;
    }

    public List<Provider> searchProviders(String category, String location) {
        Provider.Category cat = null;
        if (category != null && !category.isBlank()) {
            try { cat = Provider.Category.valueOf(category.toUpperCase()); }
            catch (Exception ignored) {}
        }
        return providerRepository.searchProviders(cat, location);
    }

    @Transactional
    public Provider addProvider(Provider provider, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        provider.setAddedBy(user);
        provider.setStatus(Provider.Status.ACTIVE);
        return providerRepository.save(provider);
    }

    @Transactional
    public Provider updateProvider(Long id, Provider updated, String username) {
        Provider existing = getProviderById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setLocation(updated.getLocation());
        existing.setYearsExperience(updated.getYearsExperience());
        return providerRepository.save(existing);
    }

    @Transactional
    public void deleteProvider(Long id) {
        Provider provider = getProviderById(id);
        provider.setStatus(Provider.Status.SUSPENDED);
        providerRepository.save(provider);
    }

    public List<Provider> getAllProvidersForAdmin() {
        return providerRepository.findAll();
    }
}
