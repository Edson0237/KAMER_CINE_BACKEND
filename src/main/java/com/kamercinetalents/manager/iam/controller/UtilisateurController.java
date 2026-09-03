package com.kamercinetalents.manager.iam.controller;

import com.kamercinetalents.manager.iam.dto.ChangePasswordRequest;
import com.kamercinetalents.manager.iam.dto.CreateUtilisateurRequest;
import com.kamercinetalents.manager.iam.dto.UtilisateurDto;
import com.kamercinetalents.manager.iam.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST du module IAM — gestion des utilisateurs.
 *
 * <p>Convention OpenAPI : chaque endpoint est annoté {@code @Operation}
 * avec une description métier, et {@code @ApiResponse} pour les codes
 * de retour attendus. Ce contrôleur délègue toute la logique au service
 * et ne contient aucune logique métier (principe SRP).</p>
 */
@RestController
@RequestMapping("/api/iam/utilisateurs")
@Tag(name = "IAM — Utilisateurs", description = "Gestion des comptes utilisateurs et de l'authentification")
@SecurityRequirement(name = "bearerAuth")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    /**
     * Construit le contrôleur avec le service injecté (principe DIP).
     *
     * @param utilisateurService le service de gestion des utilisateurs
     */
    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    /**
     * Crée un nouvel utilisateur.
     *
     * <p>Réservé au Comité Central (N1) qui seul peut créer des comptes
     * et assigner un rôle + territoire.</p>
     *
     * @param request les données de création validées
     * @return 201 Created avec le DTO de l'utilisateur créé
     */
    @PostMapping
    @Operation(summary = "Créer un utilisateur", description = "Crée un compte utilisateur avec rôle et territoire assignés. Réservé au Comité Central (N1).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Utilisateur créé"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "403", description = "Accès refusé — périmètre insuffisant")
    })
    public ResponseEntity<UtilisateurDto> create(@Valid @RequestBody CreateUtilisateurRequest request) {
        UtilisateurDto created = utilisateurService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Récupère un utilisateur par son identifiant.
     *
     * @param id l'UUID de l'utilisateur
     * @return 200 OK avec le DTO de l'utilisateur
     */
    @GetMapping("/{id}")
    @Operation(summary = "Consulter un utilisateur", description = "Retourne les informations publiques d'un utilisateur par son UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    public ResponseEntity<UtilisateurDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(utilisateurService.getById(id));
    }

    /**
     * Récupère le profil de l'utilisateur actuellement authentifié.
     *
     * @return 200 OK avec le DTO du profil courant
     */
    @GetMapping("/me")
    @Operation(summary = "Consulter mon profil", description = "Retourne les informations du compte de l'utilisateur actuellement authentifié.")
    @ApiResponse(responseCode = "200", description = "Profil courant")
    public ResponseEntity<UtilisateurDto> getCurrentUser() {
        return ResponseEntity.ok(utilisateurService.getCurrentUser());
    }

    /**
     * Change le mot de passe de l'utilisateur actuellement authentifié.
     *
     * @param request le mot de passe actuel et le nouveau mot de passe
     * @return 200 OK si le changement a réussi
     */
    @PutMapping("/me/password")
    @Operation(
            summary = "Changer mon mot de passe",
            description = "Change le mot de passe du compte connecté (volontaire ou forcé après première " +
                    "connexion avec mot de passe temporaire). Vérifie le mot de passe actuel avant application."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mot de passe changé"),
            @ApiResponse(responseCode = "400", description = "Mot de passe actuel incorrect ou nouveau mot de passe invalide")
    })
    public ResponseEntity<Void> changeOwnPassword(@Valid @RequestBody ChangePasswordRequest request) {
        utilisateurService.changeOwnPassword(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Liste les utilisateurs du périmètre de l'utilisateur connecté.
     *
     * <p>Le filtrage territorial est appliqué côté service selon le
     * contexte de sécurité Spring.</p>
     *
     * @return 200 OK avec la liste des utilisateurs visibles
     */
    @GetMapping
    @Operation(summary = "Lister les utilisateurs du périmètre", description = "Retourne les utilisateurs visibles selon le périmètre territorial de l'utilisateur connecté.")
    @ApiResponse(responseCode = "200", description = "Liste des utilisateurs")
    public ResponseEntity<List<UtilisateurDto>> list() {
        return ResponseEntity.ok(utilisateurService.listForCurrentTerritoire());
    }
}
