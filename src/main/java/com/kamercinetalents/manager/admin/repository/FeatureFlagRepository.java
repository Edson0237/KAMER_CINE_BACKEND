package com.kamercinetalents.manager.admin.repository;

import com.kamercinetalents.manager.admin.domain.FeatureFlagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data JPA pour les feature flags.
 */
public interface FeatureFlagRepository extends JpaRepository<FeatureFlagEntity, UUID> {

    /**
     * Recherche un feature flag par son code unique.
     *
     * @param code le code du flag
     * @return le flag s'il existe
     */
    Optional<FeatureFlagEntity> findByCode(String code);
}
