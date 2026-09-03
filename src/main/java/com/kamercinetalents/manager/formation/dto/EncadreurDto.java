package com.kamercinetalents.manager.formation.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record EncadreurDto(
        UUID id,
        UUID territoireId,
        String nom,
        String prenom,
        String telephone,
        String specialite,
        String disponibilite,
        BigDecimal evaluationMoyenne,
        String photoUrl,
        Map<String, Object> metadata
) {
}
