package com.kamercinetalents.manager.formation.repository;

import com.kamercinetalents.manager.formation.domain.EncadreurEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EncadreurRepository extends JpaRepository<EncadreurEntity, UUID> {
    List<EncadreurEntity> findByTerritoireId(UUID territoireId);
    long countByTerritoireId(UUID territoireId);

    /**
     * Recherche paginée des encadreurs actifs (non supprimés) d'un territoire,
     * filtrée par nom ou prénom (insensible à la casse) si {@code nom} est fourni.
     */
    @Query("SELECT e FROM EncadreurEntity e WHERE e.territoireId = :territoireId AND e.deletedAt IS NULL " +
            "AND (:nom IS NULL OR LOWER(e.nom) LIKE LOWER(CONCAT('%', :nom, '%')) OR LOWER(e.prenom) LIKE LOWER(CONCAT('%', :nom, '%')))")
    Page<EncadreurEntity> search(@Param("territoireId") UUID territoireId, @Param("nom") String nom, Pageable pageable);
}
