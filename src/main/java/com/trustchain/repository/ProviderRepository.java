package com.trustchain.repository;

import com.trustchain.model.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {
    List<Provider> findByCategory(Provider.Category category);
    List<Provider> findByLocationContainingIgnoreCase(String location);
    List<Provider> findByCategoryAndLocationContainingIgnoreCase(Provider.Category category, String location);

    @Query("SELECT p FROM Provider p WHERE " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:location IS NULL OR LOWER(p.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "p.status = 'ACTIVE' ORDER BY p.trustScore DESC")
    List<Provider> searchProviders(@Param("category") Provider.Category category,
                                   @Param("location") String location);

    List<Provider> findByStatusOrderByTrustScoreDesc(Provider.Status status);

    @Query("SELECT COUNT(p) FROM Provider p WHERE p.status = :status")
    Long countByStatus(@Param("status") Provider.Status status);
}
