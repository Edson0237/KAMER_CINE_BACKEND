package com.kamercinetalents.manager.formation.dto;

import java.time.LocalDate;
import java.util.UUID;

public record SessionFormationDto(
        UUID id,
        UUID territoireId,
        UUID encadreurId,
        LocalDate dateDebut,
        LocalDate dateFin,
        String lieu,
        String programme,
        String statut
) {
}
