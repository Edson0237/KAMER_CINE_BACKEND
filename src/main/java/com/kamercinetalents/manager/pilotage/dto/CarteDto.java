package com.kamercinetalents.manager.pilotage.dto;

import com.kamercinetalents.manager.territoire.dto.CommuneDto;
import java.util.List;

/**
 * Données de la carte interactive (M4) — communes avec statut et compteurs.
 *
 * @param communes la liste des communes du périmètre
 */
public record CarteDto(
        List<CommuneDto> communes
) {
}
