package com.kamercinetalents.manager.formation.controller;

import com.kamercinetalents.manager.common.dto.PageResponseDto;
import com.kamercinetalents.manager.formation.dto.*;
import com.kamercinetalents.manager.formation.service.FormationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST du module M3 Formation.
 *
 * <p>Expose les endpoints pour gérer les apprenants, encadreurs, sessions,
 * inscriptions, présences, résultats d'examens et attestations.
 * Le contrôle du périmètre territorial est appliqué à chaque endpoint.</p>
 */
@RestController
@RequestMapping("/api/formation")
@Tag(name = "M3 — Formation", description = "Gestion des apprenants, encadreurs, sessions, présences, résultats et attestations")
@SecurityRequirement(name = "bearerAuth")
public class FormationController {

    private final FormationService formationService;

    public FormationController(FormationService formationService) {
        this.formationService = formationService;
    }

    // ==================== APPRENANT ====================

    @GetMapping("/apprenants/{id}")
    @Operation(summary = "Récupérer un apprenant", description = "Retourne les détails d'un apprenant par son UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Apprenant trouvé",
                    content = @Content(schema = @Schema(implementation = ApprenantDto.class))),
            @ApiResponse(responseCode = "403", description = "Accès hors périmètre"),
            @ApiResponse(responseCode = "404", description = "Apprenant introuvable")
    })
    public ResponseEntity<ApprenantDto> getApprenant(@Parameter(description = "UUID de l'apprenant") @PathVariable UUID id) {
        return ResponseEntity.ok(formationService.getApprenant(id));
    }

    @GetMapping("/apprenants")
    @Operation(summary = "Lister les apprenants d'un territoire", description = "Retourne les apprenants actifs (non supprimés) du territoire spécifié.")
    @ApiResponse(responseCode = "200", description = "Liste des apprenants")
    public ResponseEntity<List<ApprenantDto>> listApprenants(
            @Parameter(description = "UUID du territoire") @RequestParam UUID territoireId) {
        return ResponseEntity.ok(formationService.listApprenantsByTerritoire(territoireId));
    }

    @PostMapping("/apprenants")
    @Operation(
            summary = "Créer un apprenant",
            description = "Crée la fiche d'un nouvel apprenant rattaché à un territoire. Nécessite la permission 'apprenant:write'."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Apprenant créé",
                    content = @Content(schema = @Schema(implementation = ApprenantDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "territoireId": "550e8400-e29b-41d4-a716-446655440000",
                                      "nom": "Nkomo",
                                      "prenom": "Aline",
                                      "dateNaissance": "2005-03-15",
                                      "sexe": "F",
                                      "telephone": "+237690123456",
                                      "competences": {"acteur": true, "realisation": false}
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "403", description = "Accès hors périmètre")
    })
    public ResponseEntity<ApprenantDto> createApprenant(@Valid @RequestBody CreateApprenantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(formationService.createApprenant(request));
    }

    @PutMapping("/apprenants/{id}")
    @Operation(summary = "Modifier un apprenant", description = "Met à jour la fiche d'un apprenant existant. Nécessite la permission 'apprenant:write'.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Apprenant modifié"),
            @ApiResponse(responseCode = "403", description = "Accès hors périmètre"),
            @ApiResponse(responseCode = "404", description = "Apprenant introuvable")
    })
    public ResponseEntity<ApprenantDto> updateApprenant(@PathVariable UUID id, @Valid @RequestBody CreateApprenantRequest request) {
        return ResponseEntity.ok(formationService.updateApprenant(id, request));
    }

    @DeleteMapping("/apprenants/{id}")
    @Operation(summary = "Supprimer un apprenant (soft delete)", description = "Marque l'apprenant comme supprimé (deleted_at).")
    @ApiResponse(responseCode = "204", description = "Apprenant supprimé")
    public ResponseEntity<Void> deleteApprenant(@PathVariable UUID id) {
        formationService.softDeleteApprenant(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/apprenants/page")
    @Operation(
            summary = "Rechercher les apprenants d'un territoire (paginé)",
            description = "Retourne une page d'apprenants filtrée par nom/prénom. " +
                    "Paramètres: page (0-indexé, défaut 0), size (défaut 20), nom (optionnel)."
    )
    @ApiResponse(responseCode = "200", description = "Page d'apprenants")
    public ResponseEntity<PageResponseDto<ApprenantDto>> searchApprenants(
            @RequestParam UUID territoireId,
            @RequestParam(required = false) String nom,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(formationService.searchApprenants(territoireId, nom, page, size));
    }

    // ==================== ENCADREUR ====================

    @GetMapping("/encadreurs/{id}")
    @Operation(summary = "Récupérer un encadreur", description = "Retourne les détails d'un encadreur par son UUID.")
    public ResponseEntity<EncadreurDto> getEncadreur(@PathVariable UUID id) {
        return ResponseEntity.ok(formationService.getEncadreur(id));
    }

    @GetMapping("/encadreurs")
    @Operation(summary = "Lister les encadreurs d'un territoire")
    public ResponseEntity<List<EncadreurDto>> listEncadreurs(@RequestParam UUID territoireId) {
        return ResponseEntity.ok(formationService.listEncadreursByTerritoire(territoireId));
    }

    @PostMapping("/encadreurs")
    @Operation(
            summary = "Créer un encadreur",
            description = "Crée la fiche d'un nouvel encadreur. Nécessite la permission 'encadreur:write'."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Encadreur créé",
                    content = @Content(schema = @Schema(implementation = EncadreurDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "territoireId": "550e8400-e29b-41d4-a716-446655440000",
                                      "nom": "Atangana",
                                      "prenom": "Jean-Paul",
                                      "telephone": "+237699887766",
                                      "specialite": "Réalisation",
                                      "disponibilite": "weekend"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<EncadreurDto> createEncadreur(@Valid @RequestBody CreateEncadreurRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(formationService.createEncadreur(request));
    }

    @PutMapping("/encadreurs/{id}")
    @Operation(summary = "Modifier un encadreur", description = "Met à jour la fiche d'un encadreur existant. Nécessite la permission 'encadreur:write'.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encadreur modifié"),
            @ApiResponse(responseCode = "403", description = "Accès hors périmètre"),
            @ApiResponse(responseCode = "404", description = "Encadreur introuvable")
    })
    public ResponseEntity<EncadreurDto> updateEncadreur(@PathVariable UUID id, @Valid @RequestBody CreateEncadreurRequest request) {
        return ResponseEntity.ok(formationService.updateEncadreur(id, request));
    }

    @DeleteMapping("/encadreurs/{id}")
    @Operation(summary = "Supprimer un encadreur (soft delete)", description = "Marque l'encadreur comme supprimé (deleted_at).")
    @ApiResponse(responseCode = "204", description = "Encadreur supprimé")
    public ResponseEntity<Void> deleteEncadreur(@PathVariable UUID id) {
        formationService.softDeleteEncadreur(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/encadreurs/page")
    @Operation(
            summary = "Rechercher les encadreurs d'un territoire (paginé)",
            description = "Retourne une page d'encadreurs filtrée par nom/prénom. " +
                    "Paramètres: page (0-indexé, défaut 0), size (défaut 20), nom (optionnel)."
    )
    @ApiResponse(responseCode = "200", description = "Page d'encadreurs")
    public ResponseEntity<PageResponseDto<EncadreurDto>> searchEncadreurs(
            @RequestParam UUID territoireId,
            @RequestParam(required = false) String nom,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(formationService.searchEncadreurs(territoireId, nom, page, size));
    }

    // ==================== SESSION ====================

    @GetMapping("/sessions/{id}")
    @Operation(summary = "Récupérer une session de formation")
    public ResponseEntity<SessionFormationDto> getSession(@PathVariable UUID id) {
        return ResponseEntity.ok(formationService.getSession(id));
    }

    @GetMapping("/sessions")
    @Operation(summary = "Lister les sessions d'un territoire")
    public ResponseEntity<List<SessionFormationDto>> listSessions(@RequestParam UUID territoireId) {
        return ResponseEntity.ok(formationService.listSessionsByTerritoire(territoireId));
    }

    @PostMapping("/sessions")
    @Operation(
            summary = "Créer une session de formation",
            description = "Crée une nouvelle session animée par un encadreur. L'UUID peut être fourni côté mobile (hors-ligne). Nécessite la permission 'session:write'."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Session créée",
                    content = @Content(schema = @Schema(implementation = SessionFormationDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "770e8400-e29b-41d4-a716-446655440002",
                                      "territoireId": "550e8400-e29b-41d4-a716-446655440000",
                                      "encadreurId": "660e8400-e29b-41d4-a716-446655440001",
                                      "dateDebut": "2025-06-01",
                                      "dateFin": "2025-06-30",
                                      "lieu": "Maison des Jeunes de Yaoundé",
                                      "programme": "Initiation à la réalisation documentaire",
                                      "statut": "planifiee"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<SessionFormationDto> createSession(@Valid @RequestBody CreateSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(formationService.createSession(request));
    }

    @PutMapping("/sessions/{id}")
    @Operation(summary = "Modifier une session de formation", description = "Met à jour une session existante. Nécessite la permission 'session:write'.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session modifiée"),
            @ApiResponse(responseCode = "403", description = "Accès hors périmètre"),
            @ApiResponse(responseCode = "404", description = "Session introuvable")
    })
    public ResponseEntity<SessionFormationDto> updateSession(@PathVariable UUID id, @Valid @RequestBody CreateSessionRequest request) {
        return ResponseEntity.ok(formationService.updateSession(id, request));
    }

    @GetMapping("/sessions/page")
    @Operation(
            summary = "Rechercher les sessions d'un territoire (paginé)",
            description = "Retourne une page de sessions filtrée par lieu/programme. " +
                    "Paramètres: page (0-indexé, défaut 0), size (défaut 20), recherche (optionnel)."
    )
    @ApiResponse(responseCode = "200", description = "Page de sessions")
    public ResponseEntity<PageResponseDto<SessionFormationDto>> searchSessions(
            @RequestParam UUID territoireId,
            @RequestParam(required = false) String recherche,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(formationService.searchSessions(territoireId, recherche, page, size));
    }

    @PostMapping("/sessions/{id}/cloturer")
    @Operation(
            summary = "Clôturer une session de formation",
            description = "Marque la session comme clôturée (statut = 'cloturee'). Le taux de réussite peut alors être calculé."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session clôturée"),
            @ApiResponse(responseCode = "403", description = "Accès hors périmètre"),
            @ApiResponse(responseCode = "404", description = "Session introuvable")
    })
    public ResponseEntity<SessionFormationDto> cloturerSession(@PathVariable UUID id) {
        return ResponseEntity.ok(formationService.cloturerSession(id));
    }

    @GetMapping("/sessions/{id}/taux-reussite")
    @Operation(
            summary = "Calculer le taux de réussite d'une session",
            description = "Calcule automatiquement le taux de réussite à partir des résultats d'examens enregistrés. " +
                    "Un apprenant est considéré comme ayant réussi si sa note est >= 10/20."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Taux de réussite calculé",
                    content = @Content(schema = @Schema(implementation = TauxReussiteDto.class))),
            @ApiResponse(responseCode = "403", description = "Accès hors périmètre"),
            @ApiResponse(responseCode = "404", description = "Session introuvable")
    })
    public ResponseEntity<TauxReussiteDto> getTauxReussite(@PathVariable UUID id) {
        return ResponseEntity.ok(formationService.calculerTauxReussite(id));
    }

    // ==================== INSCRIPTION ====================

    @PostMapping("/sessions/{sessionId}/inscriptions/{apprenantId}")
    @Operation(summary = "Inscrire un apprenant à une session", description = "Crée l'association apprenant ↔ session.")
    @ApiResponse(responseCode = "204", description = "Inscription effectuée")
    public ResponseEntity<Void> inscrire(
            @PathVariable UUID sessionId,
            @PathVariable UUID apprenantId) {
        formationService.inscrireApprenant(sessionId, apprenantId);
        return ResponseEntity.noContent().build();
    }

    // ==================== PRESENCE ====================

    @PostMapping("/presences")
    @Operation(
            summary = "Saisir une présence",
            description = "Enregistre la présence d'un apprenant à une session pour une date donnée. " +
                    "Nécessite la permission 'presence:write'. Saisie par l'utilisateur connecté."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Présence enregistrée",
                    content = @Content(schema = @Schema(implementation = PresenceDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "sessionId": "770e8400-e29b-41d4-a716-446655440002",
                                      "apprenantId": "550e8400-e29b-41d4-a716-446655440000",
                                      "date": "2025-06-15",
                                      "statut": "present"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<PresenceDto> saisirPresence(@Valid @RequestBody CreatePresenceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(formationService.saisirPresence(request));
    }

    @GetMapping("/sessions/{sessionId}/presences")
    @Operation(summary = "Lister les présences d'une session")
    public ResponseEntity<List<PresenceDto>> listPresences(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(formationService.listPresencesBySession(sessionId));
    }

    // ==================== RÉSULTAT EXAMEN ====================

    @PostMapping("/resultats")
    @Operation(
            summary = "Saisir un résultat d'examen",
            description = "Enregistre la note d'un apprenant pour un examen. Nécessite la permission 'resultat:write'."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Résultat enregistré",
                    content = @Content(schema = @Schema(implementation = ResultatExamenDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "sessionId": "770e8400-e29b-41d4-a716-446655440002",
                                      "apprenantId": "550e8400-e29b-41d4-a716-446655440000",
                                      "note": 15.5,
                                      "dateExamen": "2025-06-28"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<ResultatExamenDto> saisirResultat(@Valid @RequestBody CreateResultatExamenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(formationService.saisirResultat(request));
    }

    @GetMapping("/sessions/{sessionId}/resultats")
    @Operation(summary = "Lister les résultats d'examen d'une session")
    public ResponseEntity<List<ResultatExamenDto>> listResultats(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(formationService.listResultatsBySession(sessionId));
    }

    // ==================== ATTESTATION ====================

    @PostMapping("/attestations")
    @Operation(
            summary = "Générer une attestation",
            description = "Génère une attestation pour un apprenant ayant terminé une session. " +
                    "Le numéro unique est attribué automatiquement. " +
                    "Réservé aux niveaux hiérarchiques 1 à 5 (Comité Central à Commune). " +
                    "Un encadreur (N6) ou un apprenant (N7) ne peut pas générer d'attestation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Attestation générée",
                    content = @Content(schema = @Schema(implementation = AttestationDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "apprenantId": "550e8400-e29b-41d4-a716-446655440000",
                                      "sessionId": "770e8400-e29b-41d4-a716-446655440002"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<AttestationDto> genererAttestation(
            @Parameter(description = "UUID de l'apprenant") @RequestParam UUID apprenantId,
            @Parameter(description = "UUID de la session") @RequestParam UUID sessionId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(formationService.genererAttestation(apprenantId, sessionId));
    }

    @GetMapping("/attestations/{id}")
    @Operation(summary = "Récupérer une attestation")
    public ResponseEntity<AttestationDto> getAttestation(@PathVariable UUID id) {
        return ResponseEntity.ok(formationService.getAttestation(id));
    }
}
