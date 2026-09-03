package com.kamercinetalents.manager.iam.repository;

import com.kamercinetalents.manager.iam.domain.RolePermissionEntity;
import com.kamercinetalents.manager.iam.domain.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository Spring Data JPA pour l'entité de liaison {@link RolePermissionEntity}.
 *
 * <p>Permet de gérer les associations rôle ↔ permission (RBAC dynamique) :
 * ajout, suppression, et listing des permissions par rôle.</p>
 */
public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, RolePermissionId> {

    /**
     * Liste toutes les associations de permissions pour un rôle donné.
     *
     * @param roleId l'identifiant du rôle
     * @return la liste des associations rôle-permission
     */
    List<RolePermissionEntity> findByRoleId(UUID roleId);

    /**
     * Supprime toutes les associations de permissions pour un rôle donné.
     *
     * @param roleId l'identifiant du rôle
     */
    void deleteByRoleId(UUID roleId);
}
