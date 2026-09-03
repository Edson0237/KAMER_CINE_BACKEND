package com.kamercinetalents.manager.formation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public record CreateApprenantRequest(
        UUID id,
        @NotNull UUID territoireId,
        @NotBlank @Size(max = 100) String nom,
        @NotBlank @Size(max = 100) String prenom,
        LocalDate dateNaissance,
        @Size(max = 1) String sexe,
        @Size(max = 20) String telephone,
        String photoUrl,
        Map<String, Object> competences,
        Map<String, Object> portfolio,
        Map<String, Object> metadata
) {
}
