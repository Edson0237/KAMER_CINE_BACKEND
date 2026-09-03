package com.kamercinetalents.manager.formation.repository;

import com.kamercinetalents.manager.formation.domain.PresenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PresenceRepository extends JpaRepository<PresenceEntity, UUID> {
    List<PresenceEntity> findBySessionId(UUID sessionId);
    List<PresenceEntity> findByApprenantId(UUID apprenantId);
}
