package com.kamercinetalents.manager.territoire.controller;

import com.kamercinetalents.manager.common.dto.PageResponseDto;
import com.kamercinetalents.manager.territoire.dto.CommuneDto;
import com.kamercinetalents.manager.territoire.dto.CreateTerritoireRequest;
import com.kamercinetalents.manager.territoire.dto.StatutCommuneDto;
import com.kamercinetalents.manager.territoire.dto.TerritoireDto;
import com.kamercinetalents.manager.territoire.dto.TypeTerritoireDto;
import com.kamercinetalents.manager.territoire.service.TerritoireService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
 * Contrôleur REST du module M2 — Territoire.
 *
 * <p>Expose les endpoints de gestion de la hiérarchie territoriale :
 * création, consultation, listing des enfants, suppression douce.
 * Le contrôle du périmètre territorial est appliqué à chaque endpoint
 * via {@link com.kamercinetalents.manager.common.service.TerritoireAccessService}.</p>
 */
@RestController
@RequestMapping("/api/territoires")
@Tag(name = "M2 — Territoire", description = "Gestion de la hiérarchie territoriale (Pays, Région, Département, Arrondissement, Commune)")
@SecurityRequirement(name = "bearerAuth")
public class TerritoireController {

    private final TerritoireService territoireService;

    /**
     * Construit le contrôleur avec le service injecté.
     *
     * @param territoireService le service de gestion des territoires
     */
    public TerritoireController(TerritoireService territoireService) {
        this.territoireService = territoireService;
    }

    /**
     * Crée un nouveau territoire dans la hiérarchie.
     *
     * @param request les données de création
     * @return 201 Created avec le DTO du territoire créé
     */
    @PostMapping
    @Operation(
            summary = "Créer un territoire",
            description = "Crée un nouveau territoire dans la hiérarchie. Le territoire parent " +
                    "doit être dans le périmètre de l'utilisateur courant. " +
                    "Nécessite la permission 'territoire:write'. L'action est journalisée dans audit_log."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Territoire créé",
                    content = @Content(
                            schema = @Schema(implementation = TerritoireDto.class),
                            examples = @ExampleObject(
                                    name = "territoireCree",
                                    summary = "Réponse de création d'une région",
                                    value = """
                                            {
                                              "id": "cd34e5f6-7890-abcd-ef01-234567890abc",
                                              "code": "REG_CENTRE",
                                              "nom": "Région du Centre",
                                              "typeTerritoireId": "bb22c3d4-e5f6-7890-abcd-ef0123456780",
                                              "parentId": "aa11b2c3-d4e5-6789-abcd-ef0123456788",
                                              "statutCommuneId": null,
                                              "metadata": null,
                                              "deletedAt": null
                                            }
                                            """
                            )
                    )),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "403", description = "Accès refusé — périmètre insuffisant")
    })
    public ResponseEntity<TerritoireDto> create(@Valid @RequestBody CreateTerritoireRequest request) {
        TerritoireDto created = territoireService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Liste tous les territoires du périmètre de l'utilisateur courant.
     *
     * @return 200 OK avec la liste des territoires
     */
    @GetMapping
    @Operation(
            summary = "Lister les territoires",
            description = "Retourne tous les territoires du périmètre de l'utilisateur courant."
    )
    @ApiResponse(responseCode = "200", description = "Liste des territoires")
    public ResponseEntity<List<TerritoireDto>> listAll() {
        return ResponseEntity.ok(territoireService.listAll());
    }

    /**
     * Liste les communes du périmètre avec compteurs de déploiement.
     *
     * @return 200 OK avec la liste des communes
     */
    @GetMapping("/communes")
    @Operation(
            summary = "Lister les communes du périmètre",
            description = "Retourne les communes du périmètre de l'utilisateur avec leur statut " +
                    "de déploiement et les compteurs (apprenants, encadreurs, sessions)."
    )
    @ApiResponse(responseCode = "200", description = "Liste des communes")
    public ResponseEntity<List<CommuneDto>> getCommunes() {
        return ResponseEntity.ok(territoireService.getCommunes());
    }

    /**
     * Recherche paginée des communes du périmètre, filtrée par nom.
     *
     * @param nom  filtre optionnel par nom de commune
     * @param page le numéro de page (0-indexé, défaut 0)
     * @param size la taille de page (défaut 20)
     * @return 200 OK avec la page de communes
     */
    @GetMapping("/communes/page")
    @Operation(
            summary = "Rechercher les communes du périmètre (paginé)",
            description = "Retourne une page de communes filtrée par nom. " +
                    "Paramètres: page (0-indexé, défaut 0), size (défaut 20), nom (optionnel)."
    )
    @ApiResponse(responseCode = "200", description = "Page de communes")
    public ResponseEntity<PageResponseDto<CommuneDto>> getCommunesPage(
            @RequestParam(required = false) String nom,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(territoireService.getCommunesPage(nom, page, size));
    }

    /**
     * Récupère le détail d'une commune par son identifiant.
     *
     * @param id l'UUID de la commune
     * @return 200 OK avec le DTO de la commune
     */
    @GetMapping("/communes/{id}")
    @Operation(
            summary = "Consulter une commune",
            description = "Retourne les informations détaillées d'une commune avec compteurs."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Commune trouvée",
                    content = @Content(schema = @Schema(implementation = CommuneDto.class))),
            @ApiResponse(responseCode = "403", description = "Accès refusé — périmètre insuffisant"),
            @ApiResponse(responseCode = "404", description = "Commune introuvable")
    })
    public ResponseEntity<CommuneDto> getCommuneById(@Parameter(description = "UUID de la commune") @PathVariable UUID id) {
        return ResponseEntity.ok(territoireService.getCommuneById(id));
    }

    /**
     * Récupère un territoire par son identifiant.
     *
     * @param id l'UUID du territoire
     * @return 200 OK avec le DTO du territoire
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Consulter un territoire",
            description = "Retourne les informations d'un territoire par son UUID. " +
                    "Le territoire doit être dans le périmètre de l'utilisateur courant."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Territoire trouvé",
                    content = @Content(schema = @Schema(implementation = TerritoireDto.class))),
            @ApiResponse(responseCode = "403", description = "Accès refusé — périmètre insuffisant"),
            @ApiResponse(responseCode = "404", description = "Territoire introuvable")
    })
    public ResponseEntity<TerritoireDto> getById(@Parameter(description = "UUID du territoire") @PathVariable UUID id) {
        return ResponseEntity.ok(territoireService.getById(id));
    }

    /**
     * Liste les enfants directs d'un territoire parent.
     *
     * @param parentId l'UUID du parent
     * @return 200 OK avec la liste des enfants
     */
    @GetMapping("/{parentId}/children")
    @Operation(
            summary = "Lister les enfants d'un territoire",
            description = "Retourne les territoires enfants directs d'un parent. " +
                    "Le parent doit être dans le périmètre de l'utilisateur courant."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des enfants"),
            @ApiResponse(responseCode = "403", description = "Accès refusé — périmètre insuffisant")
    })
    public ResponseEntity<List<TerritoireDto>> getChildren(
            @Parameter(description = "UUID du territoire parent") @PathVariable UUID parentId) {
        return ResponseEntity.ok(territoireService.getChildren(parentId));
    }

    /**
     * Liste tous les types de territoire (table de référence).
     *
     * @return 200 OK avec la liste des types
     */
    @GetMapping("/types")
    @Operation(
            summary = "Lister les types de territoire",
            description = "Retourne tous les types de territoire (Pays, Région, Département, etc.)."
    )
    @ApiResponse(responseCode = "200", description = "Liste des types")
    public ResponseEntity<List<TypeTerritoireDto>> getTypes() {
        return ResponseEntity.ok(territoireService.getAllTypes());
    }

    /**
     * Liste tous les statuts de commune (table de référence).
     *
     * @return 200 OK avec la liste des statuts
     */
    @GetMapping("/statuts-commune")
    @Operation(
            summary = "Lister les statuts de commune",
            description = "Retourne tous les statuts de commune (Actif, Suspendu, etc.)."
    )
    @ApiResponse(responseCode = "200", description = "Liste des statuts")
    public ResponseEntity<List<StatutCommuneDto>> getStatuts() {
        return ResponseEntity.ok(territoireService.getAllStatuts());
    }

    /**
     * Effectue une suppression douce d'un territoire.
     *
     * @param id l'UUID du territoire
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Supprimer un territoire (soft delete)",
            description = "Marque un territoire comme supprimé (deleted_at). " +
                    "Le territoire doit être dans le périmètre de l'utilisateur courant. " +
                    "Nécessite la permission 'territoire:write'. L'action est journalisée."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Suppression enregistrée"),
            @ApiResponse(responseCode = "403", description = "Accès refusé — périmètre insuffisant"),
            @ApiResponse(responseCode = "404", description = "Territoire introuvable")
    })
    public ResponseEntity<Void> softDelete(@Parameter(description = "UUID du territoire") @PathVariable UUID id) {
        territoireService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
