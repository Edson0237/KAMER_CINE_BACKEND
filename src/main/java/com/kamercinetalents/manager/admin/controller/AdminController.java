package com.kamercinetalents.manager.admin.controller;

import com.kamercinetalents.manager.admin.dto.AuditLogDto;
import com.kamercinetalents.manager.admin.dto.FeatureFlagDto;
import com.kamercinetalents.manager.admin.dto.ParametreSystemeDto;
import com.kamercinetalents.manager.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Contrôleur REST du module M0 — Administration & Supervision.
 *
 * <p>Expose les endpoints de consultation du journal d'audit, de gestion
 * des paramètres système et des feature flags. Tous les endpoints
 * nécessitent une authentification JWT et la permission appropriée
 * (RBAC dynamique vérifié au niveau service).</p>
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "M0 — Administration", description = "Paramètres système, feature flags, journal d'audit")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    /**
     * Construit le contrôleur avec le service injecté.
     *
     * @param adminService le service d'administration
     */
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Consulte le journal d'audit avec filtres optionnels.
     *
     * @param utilisateurId filtre par utilisateur
     * @param entiteType     filtre par type d'entité
     * @param page           numéro de page (0-based)
     * @param size           taille de page
     * @return une page d'entrées d'audit
     */
    @GetMapping("/audit")
    @Operation(
            summary = "Consulter le journal d'audit",
            description = "Retourne les entrées du journal d'audit avec filtres optionnels " +
                    "par utilisateur et type d'entité. Nécessite la permission 'audit:read'."
    )
    @ApiResponse(responseCode = "200", description = "Page d'entrées d'audit",
            content = @Content(schema = @Schema(implementation = AuditLogDto.class)))
    @ApiResponse(responseCode = "403", description = "Accès refusé — permission insuffisante")
    public ResponseEntity<Page<AuditLogDto>> getAuditLog(
            @Parameter(description = "Filtre par utilisateur") @RequestParam(required = false) UUID utilisateurId,
            @Parameter(description = "Filtre par type d'entité") @RequestParam(required = false) String entiteType,
            @Parameter(description = "Numéro de page") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de page") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminService.getAuditLog(utilisateurId, entiteType, pageable));
    }

    /**
     * Liste tous les paramètres système.
     *
     * @return la liste des paramètres
     */
    @GetMapping("/parametres")
    @Operation(
            summary = "Lister les paramètres système",
            description = "Retourne tous les paramètres système configurables. " +
                    "Nécessite la permission 'parametre:read'."
    )
    @ApiResponse(responseCode = "200", description = "Liste des paramètres")
    public ResponseEntity<List<ParametreSystemeDto>> getParametres() {
        return ResponseEntity.ok(adminService.getAllParametres());
    }

    /**
     * Modifie la valeur d'un paramètre système.
     *
     * @param cle     la clé du paramètre
     * @param body    le corps contenant la nouvelle valeur
     * @return le paramètre mis à jour
     */
    @PutMapping("/parametres/{cle}")
    @Operation(
            summary = "Modifier un paramètre système",
            description = "Met à jour la valeur d'un paramètre système. " +
                    "Nécessite la permission 'parametre:write'. L'action est journalisée dans audit_log."
    )
    @ApiResponse(responseCode = "200", description = "Paramètre mis à jour")
    @ApiResponse(responseCode = "404", description = "Paramètre introuvable")
    public ResponseEntity<ParametreSystemeDto> updateParametre(
            @Parameter(description = "Clé du paramètre") @PathVariable String cle,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(adminService.updateParametre(cle, body.get("valeur")));
    }

    /**
     * Liste tous les feature flags.
     *
     * @return la liste des feature flags
     */
    @GetMapping("/feature-flags")
    @Operation(
            summary = "Lister les feature flags",
            description = "Retourne tous les feature flags. " +
                    "Nécessite la permission 'feature_flag:read'."
    )
    @ApiResponse(responseCode = "200", description = "Liste des feature flags")
    public ResponseEntity<List<FeatureFlagDto>> getFeatureFlags() {
        return ResponseEntity.ok(adminService.getAllFeatureFlags());
    }

    /**
     * Active ou désactive un feature flag.
     *
     * @param code  le code du flag
     * @param body  le corps contenant l'état
     * @return le flag mis à jour
     */
    @PutMapping("/feature-flags/{code}/toggle")
    @Operation(
            summary = "Activer/désactiver un feature flag",
            description = "Bascule l'état d'un feature flag. " +
                    "Nécessite la permission 'feature_flag:write'. L'action est journalisée."
    )
    @ApiResponse(responseCode = "200", description = "Feature flag mis à jour")
    @ApiResponse(responseCode = "404", description = "Feature flag introuvable")
    public ResponseEntity<FeatureFlagDto> toggleFeatureFlag(
            @Parameter(description = "Code du flag") @PathVariable String code,
            @RequestBody Map<String, Boolean> body) {
        return ResponseEntity.ok(adminService.toggleFeatureFlag(code, body.get("actif")));
    }
}
