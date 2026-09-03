package com.kamercinetalents.manager.iam.service;

import com.kamercinetalents.manager.common.service.AuditService;
import com.kamercinetalents.manager.iam.domain.PermissionEntity;
import com.kamercinetalents.manager.iam.domain.RoleEntity;
import com.kamercinetalents.manager.iam.domain.RolePermissionEntity;
import com.kamercinetalents.manager.iam.domain.RolePermissionId;
import com.kamercinetalents.manager.iam.dto.PermissionDto;
import com.kamercinetalents.manager.iam.dto.RoleDto;
import com.kamercinetalents.manager.iam.repository.PermissionRepository;
import com.kamercinetalents.manager.iam.repository.RolePermissionRepository;
import com.kamercinetalents.manager.iam.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service de gestion des rôles et permissions (RBAC dynamique).
 *
 * <p>Permet au Comité Central (N1) de :
 * <ul>
 *   <li>Créer et lister les rôles</li>
 *   <li>Créer et lister les permissions</li>
 *   <li>Assigner ou retirer des permissions à un rôle</li>
 * </ul>
 * Toutes les opérations CUD sont journalisées dans audit_log.</p>
 */
@Service
@Transactional
public class RolePermissionService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final AuditService auditService;

    /**
     * Construit le service avec ses dépendances injectées.
     *
     * @param roleRepository           repository des rôles
     * @param permissionRepository     repository des permissions
     * @param rolePermissionRepository repository des associations rôle-permission
     * @param auditService             service d'audit transverse
     */
    public RolePermissionService(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository,
            AuditService auditService) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.auditService = auditService;
    }

    /**
     * Liste tous les rôles.
     *
     * @return la liste des rôles en DTO
     */
    @Transactional(readOnly = true)
    public List<RoleDto> listRoles() {
        return roleRepository.findAll().stream()
                .map(r -> new RoleDto(r.getId(), r.getCode(), r.getLibelle(), r.getNiveauHierarchique()))
                .toList();
    }

    /**
     * Crée un nouveau rôle.
     *
     * @param code              le code unique du rôle
     * @param libelle           le libellé descriptif
     * @param niveauHierarchique le niveau hiérarchique (1 à 7)
     * @return le DTO du rôle créé
     */
    public RoleDto createRole(String code, String libelle, short niveauHierarchique) {
        RoleEntity entity = new RoleEntity();
        entity.setId(UUID.randomUUID());
        entity.setCode(code);
        entity.setLibelle(libelle);
        entity.setNiveauHierarchique(niveauHierarchique);
        RoleEntity saved = roleRepository.save(entity);

        auditService.log("create", "role", saved.getId(),
                Map.of("code", saved.getCode(), "niveau", saved.getNiveauHierarchique()));

        return new RoleDto(saved.getId(), saved.getCode(), saved.getLibelle(), saved.getNiveauHierarchique());
    }

    /**
     * Liste toutes les permissions.
     *
     * @return la liste des permissions en DTO
     */
    @Transactional(readOnly = true)
    public List<PermissionDto> listPermissions() {
        return permissionRepository.findAll().stream()
                .map(p -> new PermissionDto(p.getId(), p.getCode(), p.getLibelle()))
                .toList();
    }

    /**
     * Crée une nouvelle permission.
     *
     * @param code    le code unique (ex. "apprenant:write")
     * @param libelle le libellé descriptif
     * @return le DTO de la permission créée
     */
    public PermissionDto createPermission(String code, String libelle) {
        PermissionEntity entity = new PermissionEntity();
        entity.setId(UUID.randomUUID());
        entity.setCode(code);
        entity.setLibelle(libelle);
        PermissionEntity saved = permissionRepository.save(entity);

        auditService.log("create", "permission", saved.getId(),
                Map.of("code", saved.getCode()));

        return new PermissionDto(saved.getId(), saved.getCode(), saved.getLibelle());
    }

    /**
     * Assigne une permission à un rôle.
     *
     * @param roleId       l'UUID du rôle
     * @param permissionId l'UUID de la permission
     */
    public void assignPermission(UUID roleId, UUID permissionId) {
        RolePermissionEntity entity = new RolePermissionEntity();
        entity.setRoleId(roleId);
        entity.setPermissionId(permissionId);
        rolePermissionRepository.save(entity);

        auditService.log("assign", "role_permission", roleId,
                Map.of("role_id", roleId.toString(), "permission_id", permissionId.toString()));
    }

    /**
     * Retire une permission d'un rôle.
     *
     * @param roleId       l'UUID du rôle
     * @param permissionId l'UUID de la permission
     */
    public void removePermission(UUID roleId, UUID permissionId) {
        RolePermissionId id = new RolePermissionId(roleId, permissionId);
        rolePermissionRepository.deleteById(id);

        auditService.log("remove", "role_permission", roleId,
                Map.of("role_id", roleId.toString(), "permission_id", permissionId.toString()));
    }

    /**
     * Liste les permissions assignées à un rôle.
     *
     * @param roleId l'UUID du rôle
     * @return la liste des codes de permissions
     */
    @Transactional(readOnly = true)
    public List<String> getPermissionCodesForRole(UUID roleId) {
        return permissionRepository.findPermissionCodesByRoleId(roleId);
    }
}
