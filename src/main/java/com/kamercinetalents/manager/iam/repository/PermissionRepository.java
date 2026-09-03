package com.kamercinetalents.manager.iam.repository;

import com.kamercinetalents.manager.iam.domain.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Repository Spring Data JPA pour l'entité {@link PermissionEntity}.
 *
 * <p>Fournit une méthode pour charger toutes les permissions d'un rôle
 * via la table de liaison {@code role_permission}, utilisée lors de
 * la génération du JWT (RBAC dynamique).</p>
 */
public interface PermissionRepository extends JpaRepository<PermissionEntity, UUID> {

    /**
     * Charge tous les codes de permissions associés à un rôle.
     *
     * @param roleId l'identifiant du rôle
     * @return la liste des codes de permissions
     */
    @Query("SELECT p.code FROM PermissionEntity p " +
           "JOIN RolePermissionEntity rp ON rp.permissionId = p.id " +
           "WHERE rp.roleId = :roleId")
    List<String> findPermissionCodesByRoleId(@Param("roleId") UUID roleId);
}
