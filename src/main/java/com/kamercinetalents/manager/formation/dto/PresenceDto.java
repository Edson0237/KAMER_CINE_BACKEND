package com.kamercinetalents.manager.formation.dto;

import java.time.LocalDate;
import java.util.UUID;

public record PresenceDto(
        UUID id,
        UUID sessionId,
        UUID apprenantId,
        LocalDate date,
        String statut,
        UUID saisieParId
) {
}
