package com.kamercinetalents.manager.formation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

public record CreateEncadreurRequest(
        UUID id,
        @NotNull UUID territoireId,
        @NotBlank @Size(max = 100) String nom,
        @NotBlank @Size(max = 100) String prenom,
        @Size(max = 20) String telephone,
        @Size(max = 100) String specialite,
        @Size(max = 50) String disponibilite,
        String photoUrl,
        Map<String, Object> metadata
) {
}
