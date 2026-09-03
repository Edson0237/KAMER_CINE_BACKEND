package com.kamercinetalents.manager.iam.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entité JPA représentant l'association N-N entre un rôle et une permission.
 *
 * <p>Table de liaison pure — la clé primaire est composite
 * (role_id, permission_id). Permet un RBAC fin sans dupliquer la logique
 * dans le code : ajouter une permission à un rôle = un INSERT.</p>
 */
@Entity
@Table(name = "role_permission")
@IdClass(RolePermissionId.class)
public class RolePermissionEntity {

    @Id
    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Id
    @Column(name = "permission_id", nullable = false)
    private UUID permissionId;

    public RolePermissionEntity() {
    }

    public UUID getRoleId() { return roleId; }
    public void setRoleId(UUID roleId) { this.roleId = roleId; }
    public UUID getPermissionId() { return permissionId; }
    public void setPermissionId(UUID permissionId) { this.permissionId = permissionId; }
}
