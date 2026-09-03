package com.kamercinetalents.manager.formation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreatePresenceRequest(
        UUID id,
        @NotNull UUID sessionId,
        @NotNull UUID apprenantId,
        @NotNull LocalDate date,
        @NotBlank @Size(max = 20) String statut
) {
}
