package com.kamercinetalents.manager.admin.repository;

import com.kamercinetalents.manager.admin.domain.ParametreSystemeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data JPA pour les paramètres système.
 */
public interface ParametreSystemeRepository extends JpaRepository<ParametreSystemeEntity, UUID> {

    /**
     * Recherche un paramètre par sa clé unique.
     *
     * @param cle la clé du paramètre
     * @return le paramètre s'il existe
     */
    Optional<ParametreSystemeEntity> findByCle(String cle);
}
