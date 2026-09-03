package com.kamercinetalents.manager.formation.dto;

import java.time.LocalDate;
import java.util.UUID;

public record AttestationDto(
        UUID id,
        UUID apprenantId,
        UUID sessionId,
        String numero,
        LocalDate dateDelivrance,
        String fichierUrl
) {
}
