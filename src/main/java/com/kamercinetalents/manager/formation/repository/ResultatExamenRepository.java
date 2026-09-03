package com.kamercinetalents.manager.formation.repository;

import com.kamercinetalents.manager.formation.domain.ResultatExamenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ResultatExamenRepository extends JpaRepository<ResultatExamenEntity, UUID> {
    List<ResultatExamenEntity> findBySessionId(UUID sessionId);
    List<ResultatExamenEntity> findByApprenantId(UUID apprenantId);
}
