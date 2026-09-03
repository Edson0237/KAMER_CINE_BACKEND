package com.kamercinetalents.manager.formation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreateSessionRequest(
        UUID id,
        @NotNull UUID territoireId,
        UUID encadreurId,
        LocalDate dateDebut,
        LocalDate dateFin,
        @NotBlank @Size(max = 200) String lieu,
        @Size(max = 1000) String programme,
        @Size(max = 30) String statut
) {
}
