package com.kamercinetalents.manager.admin.repository;

import com.kamercinetalents.manager.admin.domain.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

/**
 * Repository Spring Data JPA pour l'entité {@link AuditLogEntity}.
 *
 * <p>Fournit des méthodes de consultation filtrable par utilisateur,
 * type d'entité et plage de dates. L'insertion se fait via
 * {@link com.kamercinetalents.manager.common.service.AuditService},
 * pas par ce repository.</p>
 */
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {

    /**
     * Recherche filtrée dans le journal d'audit.
     *
     * @param utilisateurId filtre par utilisateur (null = tous)
     * @param entiteType     filtre par type d'entité (null = tous)
     * @param pageable       pagination
     * @return une page d'entrées d'audit
     */
    @Query("SELECT a FROM AuditLogEntity a WHERE " +
           "(:utilisateurId IS NULL OR a.utilisateurId = :utilisateurId) AND " +
           "(:entiteType IS NULL OR a.entiteType = :entiteType) " +
           "ORDER BY a.date DESC")
    Page<AuditLogEntity> findFiltered(
            @Param("utilisateurId") UUID utilisateurId,
            @Param("entiteType") String entiteType,
            Pageable pageable);
}
