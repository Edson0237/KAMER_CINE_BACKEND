package com.kamercinetalents.manager.formation.repository;

import com.kamercinetalents.manager.formation.domain.AttestationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AttestationRepository extends JpaRepository<AttestationEntity, UUID> {
    Optional<AttestationEntity> findByNumero(String numero);
}
