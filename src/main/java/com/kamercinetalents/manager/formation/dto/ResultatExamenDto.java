package com.kamercinetalents.manager.formation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ResultatExamenDto(
        UUID id,
        UUID sessionId,
        UUID apprenantId,
        BigDecimal note,
        LocalDate dateExamen
) {
}
