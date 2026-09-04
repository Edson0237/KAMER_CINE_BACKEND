package com.kamercinetalents.manager.ecosysteme.controller;

import com.kamercinetalents.manager.ecosysteme.dto.*;
import com.kamercinetalents.manager.ecosysteme.service.EcosystemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST public — endpoints sans authentification pour le site vitrine.
 *
 * <p>Expose en lecture : actualités publiées, FAQ active, équipe, partenaires.
 * Expose en écriture : candidature publique et message de contact (avec
 * limite de débit gérée par {@link com.kamercinetalents.manager.common.security.RateLimitFilter}).</p>
 */
@RestController
@RequestMapping("/api/public")
@Tag(name = "Écosystème — Public", description = "Endpoints publics du site vitrine KCT")
public class PublicEcosystemeController {

    private final EcosystemeService service;

    public PublicEcosystemeController(EcosystemeService service) {
        this.service = service;
    }

    // ==================== LECTURE ====================

    @GetMapping("/actualites")
    @Operation(summary = "Lister les actualités publiées",
            description = "Retourne toutes les actualités avec le statut 'publiee'.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ActualitePubliqueDto.class)))
    public ResponseEntity<List<ActualitePubliqueDto>> getPublishedActualites() {
        return ResponseEntity.ok(service.getPublishedActualites());
    }

    @GetMapping("/actualites/{id}")
    @Operation(summary = "Consulter une actualité publiée")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ActualitePubliqueDto.class)))
    @ApiResponse(responseCode = "404", description = "Actualité introuvable ou non publiée")
    public ResponseEntity<ActualitePubliqueDto> getActualite(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getActualiteById(id));
    }

    @GetMapping("/faq")
    @Operation(summary = "Lister la FAQ active",
            description = "Retourne toutes les questions fréquentes actives, groupées par catégorie.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = FaqItemDto.class)))
    public ResponseEntity<List<FaqItemDto>> getFaq() {
        return ResponseEntity.ok(service.getActiveFaq());
    }

    @GetMapping("/equipe")
    @Operation(summary = "Lister les membres de l'équipe")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = MembreEquipeDto.class)))
    public ResponseEntity<List<MembreEquipeDto>> getEquipe() {
        return ResponseEntity.ok(service.getEquipe());
    }

    @GetMapping("/partenaires")
    @Operation(summary = "Lister les partenaires")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PartenaireDto.class)))
    public ResponseEntity<List<PartenaireDto>> getPartenaires() {
        return ResponseEntity.ok(service.getPartenaires());
    }

    @GetMapping("/evenements")
    @Operation(summary = "Lister les événements programmés")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = EvenementDto.class)))
    public ResponseEntity<List<EvenementDto>> getEvenements() {
        return ResponseEntity.ok(service.getPublishedEvenements());
    }

    @GetMapping("/evenements/{id}")
    @Operation(summary = "Consulter un événement")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = EvenementDto.class)))
    @ApiResponse(responseCode = "404", description = "Événement introuvable")
    public ResponseEntity<EvenementDto> getEvenement(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getEvenementById(id));
    }

    // ==================== ÉCRITURE ====================

    @PostMapping("/candidatures")
    @Operation(summary = "Soumettre une candidature publique",
            description = "Crée une candidature au programme. Limite de débit appliquée pour éviter le spam.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CandidaturePubliqueDto.class)))
    @ApiResponse(responseCode = "400", description = "Données invalides")
    public ResponseEntity<CandidaturePubliqueDto> soumettreCandidature(
            @Valid @RequestBody CreateCandidatureRequest request) {
        return ResponseEntity.ok(service.soumettreCandidature(request));
    }

    @PostMapping("/contact")
    @Operation(summary = "Envoyer un message de contact",
            description = "Crée un message depuis le formulaire de contact public. Limite de débit appliquée.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ContactMessageDto.class)))
    @ApiResponse(responseCode = "400", description = "Données invalides")
    public ResponseEntity<ContactMessageDto> soumettreMessage(
            @Valid @RequestBody CreateContactMessageRequest request) {
        return ResponseEntity.ok(service.soumettreMessage(request));
    }
}
