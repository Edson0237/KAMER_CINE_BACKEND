package com.kamercinetalents.manager.iam.repository;

import com.kamercinetalents.manager.iam.domain.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data JPA pour l'entité {@link RoleEntity}.
 */
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

    /**
     * Recherche un rôle par son code unique.
     *
     * @param code le code du rôle (ex. {@code N1_COMITE_CENTRAL})
     * @return le rôle s'il existe
     */
    Optional<RoleEntity> findByCode(String code);
}
