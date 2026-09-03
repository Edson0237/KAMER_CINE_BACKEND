package com.kamercinetalents.manager.formation.repository;

import com.kamercinetalents.manager.formation.domain.SessionFormationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SessionFormationRepository extends JpaRepository<SessionFormationEntity, UUID> {
    List<SessionFormationEntity> findByTerritoireId(UUID territoireId);
    List<SessionFormationEntity> findByEncadreurId(UUID encadreurId);
    long countByTerritoireId(UUID territoireId);

    /**
     * Recherche paginée des sessions d'un territoire, filtrée par lieu ou
     * programme (insensible à la casse) si {@code recherche} est fourni.
     */
    @Query("SELECT s FROM SessionFormationEntity s WHERE s.territoireId = :territoireId " +
            "AND (:recherche IS NULL OR LOWER(s.lieu) LIKE LOWER(CONCAT('%', :recherche, '%')) OR LOWER(s.programme) LIKE LOWER(CONCAT('%', :recherche, '%')))")
    Page<SessionFormationEntity> search(@Param("territoireId") UUID territoireId, @Param("recherche") String recherche, Pageable pageable);
}
