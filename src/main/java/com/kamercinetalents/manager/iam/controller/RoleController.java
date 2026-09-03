package com.kamercinetalents.manager.iam.controller;

import com.kamercinetalents.manager.common.exception.PerimeterAccessException;
import com.kamercinetalents.manager.common.security.SecurityUtils;
import com.kamercinetalents.manager.iam.dto.PermissionDto;
import com.kamercinetalents.manager.iam.dto.RoleDto;
import com.kamercinetalents.manager.iam.service.RolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST de gestion des rôles et permissions (RBAC dynamique).
 *
 * <p>Expose les endpoints pour créer et lister les rôles, créer et lister
 * les permissions, et assigner ou retirer des permissions à un rôle.
 * Réservé au Comité Central (N1) — permission {@code role:write}.</p>
 */
@RestController
@RequestMapping("/api/iam/roles")
@Tag(name = "M1 — RBAC", description = "Gestion des rôles et permissions (RBAC dynamique)")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final RolePermissionService rolePermissionService;

    /**
     * Construit le contrôleur avec le service injecté.
     *
     * @param rolePermissionService le service de gestion RBAC
     */
    public RoleController(RolePermissionService rolePermissionService) {
        this.rolePermissionService = rolePermissionService;
    }

    /**
     * Liste tous les rôles.
     *
     * @return 200 OK avec la liste des rôles
     */
    @GetMapping
    @Operation(summary = "Lister les rôles", description = "Retourne tous les rôles du système.")
    @ApiResponse(responseCode = "200", description = "Liste des rôles")
    public ResponseEntity<List<RoleDto>> listRoles() {
        return ResponseEntity.ok(rolePermissionService.listRoles());
    }

    /**
     * Crée un nouveau rôle.
     *
     * @param request les données du rôle
     * @return 201 Created avec le DTO du rôle créé
     */
    @PostMapping
    @Operation(
            summary = "Créer un rôle",
            description = "Crée un nouveau rôle avec un code unique, un libellé et un niveau hiérarchique (1 à 7). " +
                    "Réservé au Comité Central (N1)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Rôle créé",
                    content = @Content(schema = @Schema(implementation = RoleDto.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "403", description = "Accès réservé au Comité Central (N1)")
    })
    public ResponseEntity<RoleDto> createRole(@Valid @RequestBody CreateRoleRequest request) {
        requireN1();
        RoleDto created = rolePermissionService.createRole(
                request.code(), request.libelle(), request.niveauHierarchique());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Liste toutes les permissions.
     *
     * @return 200 OK avec la liste des permissions
     */
    @GetMapping("/permissions")
    @Operation(summary = "Lister les permissions", description = "Retourne toutes les permissions du système.")
    @ApiResponse(responseCode = "200", description = "Liste des permissions")
    public ResponseEntity<List<PermissionDto>> listPermissions() {
        return ResponseEntity.ok(rolePermissionService.listPermissions());
    }

    /**
     * Crée une nouvelle permission.
     *
     * @param request les données de la permission
     * @return 201 Created avec le DTO de la permission créée
     */
    @PostMapping("/permissions")
    @Operation(
            summary = "Créer une permission",
            description = "Crée une nouvelle permission granulaire (ex. 'apprenant:write'). " +
                    "Réservé au Comité Central (N1)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Permission créée",
                    content = @Content(schema = @Schema(implementation = PermissionDto.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "403", description = "Accès réservé au Comité Central (N1)")
    })
    public ResponseEntity<PermissionDto> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        requireN1();
        PermissionDto created = rolePermissionService.createPermission(request.code(), request.libelle());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Assigne une permission à un rôle.
     *
     * @param roleId       l'UUID du rôle
     * @param permissionId l'UUID de la permission
     * @return 204 No Content
     */
    @PostMapping("/{roleId}/permissions/{permissionId}")
    @Operation(
            summary = "Assigner une permission à un rôle",
            description = "Associe une permission à un rôle via la table de liaison role_permission. " +
                    "Réservé au Comité Central (N1)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Permission assignée"),
            @ApiResponse(responseCode = "403", description = "Accès réservé au Comité Central (N1)"),
            @ApiResponse(responseCode = "404", description = "Rôle ou permission introuvable")
    })
    public ResponseEntity<Void> assignPermission(
            @Parameter(description = "UUID du rôle") @PathVariable UUID roleId,
            @Parameter(description = "UUID de la permission") @PathVariable UUID permissionId) {
        requireN1();
        rolePermissionService.assignPermission(roleId, permissionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retire une permission d'un rôle.
     *
     * @param roleId       l'UUID du rôle
     * @param permissionId l'UUID de la permission
     * @return 204 No Content
     */
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @Operation(
            summary = "Retirer une permission d'un rôle",
            description = "Dissocie une permission d'un rôle. Réservé au Comité Central (N1)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Permission retirée"),
            @ApiResponse(responseCode = "403", description = "Accès réservé au Comité Central (N1)"),
            @ApiResponse(responseCode = "404", description = "Association introuvable")
    })
    public ResponseEntity<Void> removePermission(
            @Parameter(description = "UUID du rôle") @PathVariable UUID roleId,
            @Parameter(description = "UUID de la permission") @PathVariable UUID permissionId) {
        requireN1();
        rolePermissionService.removePermission(roleId, permissionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Liste les permissions assignées à un rôle.
     *
     * @param roleId l'UUID du rôle
     * @return 200 OK avec la liste des codes de permissions
     */
    @GetMapping("/{roleId}/permissions")
    @Operation(summary = "Lister les permissions d'un rôle", description = "Retourne les codes de permissions assignés à un rôle.")
    @ApiResponse(responseCode = "200", description = "Liste des permissions")
    public ResponseEntity<List<String>> getRolePermissions(
            @Parameter(description = "UUID du rôle") @PathVariable UUID roleId) {
        return ResponseEntity.ok(rolePermissionService.getPermissionCodesForRole(roleId));
    }

    /**
     * DTO de requête pour la création d'un rôle.
     */
    public record CreateRoleRequest(
            @NotBlank @Size(max = 50) String code,
            @NotBlank @Size(max = 100) String libelle,
            @Min(1) @Max(7) short niveauHierarchique
    ) {
    }

    /**
     * DTO de requête pour la création d'une permission.
     */
    public record CreatePermissionRequest(
            @NotBlank @Size(max = 50) String code,
            @NotBlank @Size(max = 100) String libelle
    ) {
    }

    private void requireN1() {
        int niveau = SecurityUtils.get().niveauHierarchique();
        if (niveau != 1) {
            throw new PerimeterAccessException(
                    "Opération réservée au Comité Central (N1). Niveau actuel: N" + niveau);
        }
    }
}
