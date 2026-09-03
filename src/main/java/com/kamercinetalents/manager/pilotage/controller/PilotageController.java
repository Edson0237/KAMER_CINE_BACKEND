package com.kamercinetalents.manager.pilotage.controller;

import com.kamercinetalents.manager.pilotage.dto.CarteDto;
import com.kamercinetalents.manager.pilotage.dto.IndicateurDto;
import com.kamercinetalents.manager.pilotage.service.PilotageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Contrôleur REST du module M4 — Pilotage.
 *
 * <p>Expose les endpoints du tableau de bord (indicateurs clés) et
 * de la carte interactive (communes + statut + compteurs).</p>
 */
@RestController
@RequestMapping("/api/pilotage")
@Tag(name = "M4 — Pilotage", description = "Tableau de bord et carte interactive")
@SecurityRequirement(name = "bearerAuth")
public class PilotageController {

    private final PilotageService pilotageService;

    public PilotageController(PilotageService pilotageService) {
        this.pilotageService = pilotageService;
    }

    /**
     * Récupère les indicateurs clés du périmètre de l'utilisateur.
     *
     * @return 200 OK avec la liste des indicateurs
     */
    @GetMapping("/indicateurs")
    @Operation(
            summary = "Indicateurs du tableau de bord",
            description = "Retourne les indicateurs clés (communes actives, apprenants, encadreurs, " +
                    "sessions, taux de réussite, attestations) pour le périmètre de l'utilisateur."
    )
    @ApiResponse(responseCode = "200", description = "Liste des indicateurs")
    public ResponseEntity<List<IndicateurDto>> getIndicateurs() {
        return ResponseEntity.ok(pilotageService.getIndicateurs());
    }

    /**
     * Récupère les données de la carte interactive.
     *
     * @return 200 OK avec les données de la carte
     */
    @GetMapping("/carte")
    @Operation(
            summary = "Données de la carte",
            description = "Retourne les communes du périmètre avec leur statut de déploiement " +
                    "et les compteurs (apprenants, encadreurs, sessions)."
    )
    @ApiResponse(responseCode = "200", description = "Données de la carte")
    public ResponseEntity<CarteDto> getCarte() {
        return ResponseEntity.ok(pilotageService.getCarteData());
    }
}
