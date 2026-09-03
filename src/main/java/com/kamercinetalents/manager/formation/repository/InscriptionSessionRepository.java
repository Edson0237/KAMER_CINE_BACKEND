package com.kamercinetalents.manager.formation.repository;

import com.kamercinetalents.manager.formation.domain.InscriptionSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InscriptionSessionRepository extends JpaRepository<InscriptionSessionEntity, UUID> {
    List<InscriptionSessionEntity> findBySessionId(UUID sessionId);
    List<InscriptionSessionEntity> findByApprenantId(UUID apprenantId);
}
