package com.kamercinetalents.manager.territoire.repository;

import com.kamercinetalents.manager.territoire.domain.TypeTerritoireEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository Spring Data JPA pour les types de territoire (table de référence).
 */
public interface TypeTerritoireRepository extends JpaRepository<TypeTerritoireEntity, UUID> {
}
