package com.kamercinetalents.manager.iam.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Clé primaire composite pour {@link RolePermissionEntity}.
 */
public class RolePermissionId implements Serializable {

    private UUID roleId;
    private UUID permissionId;

    public RolePermissionId() {
    }

    public RolePermissionId(UUID roleId, UUID permissionId) {
        this.roleId = roleId;
        this.permissionId = permissionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RolePermissionId that)) return false;
        return Objects.equals(roleId, that.roleId) && Objects.equals(permissionId, that.permissionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, permissionId);
    }
}
