package com.kamercinetalents.manager.formation.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateResultatExamenRequest(
        UUID id,
        @NotNull UUID sessionId,
        @NotNull UUID apprenantId,
        @NotNull @DecimalMin("0.0") @DecimalMax("20.0") BigDecimal note,
        LocalDate dateExamen
) {
}
