package com.kamercinetalents.manager.territoire.repository;

import com.kamercinetalents.manager.territoire.domain.StatutCommuneEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository Spring Data JPA pour les statuts de commune (table de référence).
 */
public interface StatutCommuneRepository extends JpaRepository<StatutCommuneEntity, UUID> {
}
