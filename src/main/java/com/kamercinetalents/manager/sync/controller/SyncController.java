package com.kamercinetalents.manager.sync.controller;

import com.kamercinetalents.manager.sync.dto.SyncRequestDto;
import com.kamercinetalents.manager.sync.dto.SyncResponseDto;
import com.kamercinetalents.manager.sync.service.SyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la synchronisation hors-ligne — module M5.
 *
 * <p>Expose l'endpoint unique POST /api/sync qui reçoit un lot d'actions
 * depuis l'application mobile et les traite une par une avec résolution
 * Last Write Wins et journalisation des conflits.</p>
 */
@RestController
@RequestMapping("/api/sync")
@Tag(name = "M5 — Synchronisation", description = "Synchronisation hors-ligne des entités terrain")
@SecurityRequirement(name = "bearerAuth")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping
    @Operation(
            summary = "Synchroniser un lot d'actions hors-ligne",
            description = """
                    Reçoit un lot d'actions créées ou modifiées hors-ligne sur le mobile.
                    
                    **Flux (Sequence_Synchronisation_M5.puml) :**
                    1. Le mobile génère des UUIDs locaux et horodate chaque action (client_updated_at).
                    2. À la reconnexion, le mobile envoie le lot complet via cet endpoint.
                    3. Le serveur vérifie le JWT et le périmètre territorial.
                    4. Pour chaque action, dans une transaction indépendante (ACID) :
                       - **Retry** : si action.id déjà traité (statut='applied'/'conflict'), renvoi direct sans retraitement.
                       - **INSERT** : si l'entité n'existe pas côté serveur → création, statut='applied'.
                       - **UPDATE** : si server_updated_at ≤ client_updated_at → mise à jour, statut='applied'.
                       - **Conflit** : si server_updated_at > client_updated_at (modification concurrente) →
                         Last Write Wins sur horodatage_client, journalisation dans sync_conflict_log, statut='conflict'.
                       - **Rejet** : type non supporté ou attestation → statut='rejected'.
                    5. Le serveur retourne l'accusé de réception avec le statut de chaque action.
                    
                    **Entités synchronisables :** apprenant, encadreur, session_formation, presence, resultat_examen.
                    **Opérations :** create, update, delete.
                    **Non synchronisable :** attestation (endpoint serveur dédié POST /api/formation/attestations, réservé N1-N5).
                    
                    **Exemple de payload d'entrée :**
                    ```json
                    {
                      "utilisateurId": "550e8400-e29b-41d4-a716-446655440000",
                      "deviceId": "android-a1b2c3d4",
                      "actions": [
                        {
                          "id": "660e8400-e29b-41d4-a716-446655440001",
                          "entiteType": "apprenant",
                          "entiteId": "770e8400-e29b-41d4-a716-446655440002",
                          "operation": "create",
                          "horodatageClient": "2026-08-28T10:30:00+01:00",
                          "payload": {
                            "territoireId": "880e8400-e29b-41d4-a716-446655440003",
                            "nom": "Nkomo",
                            "prenom": "Aline",
                            "sexe": "F",
                            "telephone": "+237690123456"
                          }
                        },
                        {
                          "id": "660e8400-e29b-41d4-a716-446655440010",
                          "entiteType": "presence",
                          "entiteId": "990e8400-e29b-41d4-a716-446655440004",
                          "operation": "create",
                          "horodatageClient": "2026-08-28T11:00:00+01:00",
                          "payload": {
                            "sessionId": "aa0e8400-e29b-41d4-a716-446655440005",
                            "apprenantId": "770e8400-e29b-41d4-a716-446655440002",
                            "statut": "present"
                          }
                        },
                        {
                          "id": "660e8400-e29b-41d4-a716-446655440020",
                          "entiteType": "resultat_examen",
                          "entiteId": "bb0e8400-e29b-41d4-a716-446655440006",
                          "operation": "update",
                          "horodatageClient": "2026-08-28T12:00:00+01:00",
                          "payload": {
                            "sessionId": "aa0e8400-e29b-41d4-a716-446655440005",
                            "apprenantId": "770e8400-e29b-41d4-a716-446655440002",
                            "note": 14.5
                          }
                        }
                      ]
                    }
                    ```
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lot traité — accusé de réception avec statut par action",
                    content = @Content(schema = @Schema(implementation = SyncResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "totalActions": 3,
                                      "applied": 2,
                                      "conflicts": 1,
                                      "rejected": 0,
                                      "resultats": [
                                        {
                                          "id": "660e8400-e29b-41d4-a716-446655440001",
                                          "statut": "applied",
                                          "message": null
                                        },
                                        {
                                          "id": "660e8400-e29b-41d4-a716-446655440010",
                                          "statut": "applied",
                                          "message": null
                                        },
                                        {
                                          "id": "660e8400-e29b-41d4-a716-446655440020",
                                          "statut": "conflict",
                                          "message": "Résolu par Last Write Wins"
                                        }
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Requête invalide — payload manquant ou malformé"),
            @ApiResponse(responseCode = "401", description = "JWT manquant ou invalide"),
            @ApiResponse(responseCode = "403", description = "Accès hors périmètre territorial")
    })
    public ResponseEntity<SyncResponseDto> synchronize(@Valid @RequestBody SyncRequestDto request) {
        return ResponseEntity.ok(syncService.synchronize(request));
    }
}
