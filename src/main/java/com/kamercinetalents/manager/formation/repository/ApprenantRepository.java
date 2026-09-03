package com.kamercinetalents.manager.formation.repository;

import com.kamercinetalents.manager.formation.domain.ApprenantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ApprenantRepository extends JpaRepository<ApprenantEntity, UUID> {
    List<ApprenantEntity> findByTerritoireId(UUID territoireId);
    long countByTerritoireId(UUID territoireId);

    /**
     * Recherche paginée des apprenants actifs (non supprimés) d'un territoire,
     * filtrée par nom ou prénom (insensible à la casse) si {@code nom} est fourni.
     */
    @Query("SELECT a FROM ApprenantEntity a WHERE a.territoireId = :territoireId AND a.deletedAt IS NULL " +
            "AND (:nom IS NULL OR LOWER(a.nom) LIKE LOWER(CONCAT('%', :nom, '%')) OR LOWER(a.prenom) LIKE LOWER(CONCAT('%', :nom, '%')))")
    Page<ApprenantEntity> search(@Param("territoireId") UUID territoireId, @Param("nom") String nom, Pageable pageable);
}
