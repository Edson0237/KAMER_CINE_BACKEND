package com.kamercinetalents.manager.formation.dto;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public record ApprenantDto(
        UUID id,
        UUID territoireId,
        String nom,
        String prenom,
        LocalDate dateNaissance,
        String sexe,
        String telephone,
        String photoUrl,
        Map<String, Object> competences,
        Map<String, Object> portfolio,
        Map<String, Object> metadata
) {
}
