package com.kamercinetalents.manager.ecosysteme.controller;

import com.kamercinetalents.manager.ecosysteme.dto.*;
import com.kamercinetalents.manager.ecosysteme.service.EcosystemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST admin — gestion du contenu du site public.
 *
 * <p>Tous les endpoints nécessitent une authentification JWT et le
 * niveau hiérarchique 1 (Comité Central). Le contrôle est effectué
 * côté service via {@link com.kamercinetalents.manager.common.security.SecurityUtils}.</p>
 */
@RestController
@RequestMapping("/api/ecosysteme")
@Tag(name = "Écosystème — Admin", description = "Gestion du contenu du site public (Comité Central uniquement)")
@SecurityRequirement(name = "bearerAuth")
public class AdminEcosystemeController {

    private final EcosystemeService service;

    public AdminEcosystemeController(EcosystemeService service) {
        this.service = service;
    }

    // ==================== ACTUALITÉS ====================

    @GetMapping("/actualites")
    @Operation(summary = "Lister toutes les actualités (y compris brouillons)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ActualitePubliqueDto.class)))
    @ApiResponse(responseCode = "403", description = "Accès réservé au niveau 1")
    public ResponseEntity<List<ActualitePubliqueDto>> getAllActualites() {
        return ResponseEntity.ok(service.getAllActualites());
    }

    @PostMapping("/actualites")
    @Operation(summary = "Créer une actualité")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ActualitePubliqueDto.class)))
    public ResponseEntity<ActualitePubliqueDto> createActualite(@Valid @RequestBody CreateActualiteRequest req) {
        return ResponseEntity.ok(service.createActualite(req));
    }

    @PutMapping("/actualites/{id}")
    @Operation(summary = "Modifier une actualité")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ActualitePubliqueDto.class)))
    public ResponseEntity<ActualitePubliqueDto> updateActualite(
            @Parameter(description = "UUID de l'actualité") @PathVariable UUID id,
            @Valid @RequestBody CreateActualiteRequest req) {
        return ResponseEntity.ok(service.updateActualite(id, req));
    }

    @PutMapping("/actualites/{id}/toggle-publish")
    @Operation(summary = "Publier / dépublier une actualité",
            description = "Bascule le statut entre 'publiee' et 'brouillon'.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ActualitePubliqueDto.class)))
    public ResponseEntity<ActualitePubliqueDto> togglePublish(@PathVariable UUID id) {
        return ResponseEntity.ok(service.togglePublishActualite(id));
    }

    @DeleteMapping("/actualites/{id}")
    @Operation(summary = "Supprimer une actualité (suppression douce)")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteActualite(@PathVariable UUID id) {
        service.deleteActualite(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== FAQ ====================

    @GetMapping("/faq")
    @Operation(summary = "Lister toutes les questions FAQ")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = FaqItemDto.class)))
    public ResponseEntity<List<FaqItemDto>> getAllFaq() {
        return ResponseEntity.ok(service.getAllFaq());
    }

    @PostMapping("/faq")
    @Operation(summary = "Créer une question FAQ")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = FaqItemDto.class)))
    public ResponseEntity<FaqItemDto> createFaqItem(@Valid @RequestBody CreateFaqItemRequest req) {
        return ResponseEntity.ok(service.createFaqItem(req));
    }

    @PutMapping("/faq/{id}")
    @Operation(summary = "Modifier une question FAQ")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = FaqItemDto.class)))
    public ResponseEntity<FaqItemDto> updateFaqItem(@PathVariable UUID id, @Valid @RequestBody CreateFaqItemRequest req) {
        return ResponseEntity.ok(service.updateFaqItem(id, req));
    }

    @DeleteMapping("/faq/{id}")
    @Operation(summary = "Supprimer une question FAQ")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteFaqItem(@PathVariable UUID id) {
        service.deleteFaqItem(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== MEMBRES ÉQUIPE ====================

    @GetMapping("/equipe")
    @Operation(summary = "Lister tous les membres de l'équipe")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = MembreEquipeDto.class)))
    public ResponseEntity<List<MembreEquipeDto>> getAllMembres() {
        return ResponseEntity.ok(service.getAllMembres());
    }

    @PostMapping("/equipe")
    @Operation(summary = "Ajouter un membre de l'équipe")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = MembreEquipeDto.class)))
    public ResponseEntity<MembreEquipeDto> createMembre(@Valid @RequestBody CreateMembreEquipeRequest req) {
        return ResponseEntity.ok(service.createMembre(req));
    }

    @PutMapping("/equipe/{id}")
    @Operation(summary = "Modifier un membre de l'équipe")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = MembreEquipeDto.class)))
    public ResponseEntity<MembreEquipeDto> updateMembre(@PathVariable UUID id, @Valid @RequestBody CreateMembreEquipeRequest req) {
        return ResponseEntity.ok(service.updateMembre(id, req));
    }

    @DeleteMapping("/equipe/{id}")
    @Operation(summary = "Supprimer un membre de l'équipe")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteMembre(@PathVariable UUID id) {
        service.deleteMembre(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== PARTENAIRES ====================

    @GetMapping("/partenaires")
    @Operation(summary = "Lister tous les partenaires")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PartenaireDto.class)))
    public ResponseEntity<List<PartenaireDto>> getAllPartenaires() {
        return ResponseEntity.ok(service.getAllPartenaires());
    }

    @PostMapping("/partenaires")
    @Operation(summary = "Ajouter un partenaire")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PartenaireDto.class)))
    public ResponseEntity<PartenaireDto> createPartenaire(@Valid @RequestBody CreatePartenaireRequest req) {
        return ResponseEntity.ok(service.createPartenaire(req));
    }

    @PutMapping("/partenaires/{id}")
    @Operation(summary = "Modifier un partenaire")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PartenaireDto.class)))
    public ResponseEntity<PartenaireDto> updatePartenaire(@PathVariable UUID id, @Valid @RequestBody CreatePartenaireRequest req) {
        return ResponseEntity.ok(service.updatePartenaire(id, req));
    }

    @DeleteMapping("/partenaires/{id}")
    @Operation(summary = "Supprimer un partenaire")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deletePartenaire(@PathVariable UUID id) {
        service.deletePartenaire(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== CANDIDATURES ====================

    @GetMapping("/candidatures")
    @Operation(summary = "Lister toutes les candidatures publiques")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CandidaturePubliqueDto.class)))
    public ResponseEntity<List<CandidaturePubliqueDto>> getAllCandidatures() {
        return ResponseEntity.ok(service.getAllCandidatures());
    }

    @PutMapping("/candidatures/{id}/traiter")
    @Operation(summary = "Traiter une candidature (accepter ou refuser)",
            description = "Si statut='acceptee', crée un compte apprenant (N7) rattaché à la commune spécifiée, "
                    + "génère un mot de passe temporaire, et retourne les identifiants. "
                    + "Le canal de transmission (sms, email, ou afficher_ecran) est déterminé automatiquement.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CandidatureAccepteeResult.class)))
    public ResponseEntity<CandidatureAccepteeResult> traiterCandidature(
            @PathVariable UUID id,
            @RequestBody TraiterCandidatureRequest req) {
        return ResponseEntity.ok(service.traiterCandidature(id, req));
    }

    // ==================== MESSAGES DE CONTACT ====================

    @GetMapping("/contact")
    @Operation(summary = "Lister tous les messages de contact")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ContactMessageDto.class)))
    public ResponseEntity<List<ContactMessageDto>> getAllMessages() {
        return ResponseEntity.ok(service.getAllMessages());
    }

    @PutMapping("/contact/{id}/traiter")
    @Operation(summary = "Marquer un message comme traité")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ContactMessageDto.class)))
    public ResponseEntity<ContactMessageDto> marquerTraite(@PathVariable UUID id) {
        return ResponseEntity.ok(service.marquerMessageTraite(id));
    }
}
