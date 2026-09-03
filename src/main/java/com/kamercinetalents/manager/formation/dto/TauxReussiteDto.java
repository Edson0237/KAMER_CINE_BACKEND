package com.kamercinetalents.manager.formation.dto;

import java.util.UUID;

public record TauxReussiteDto(
        UUID sessionId,
        int totalApprenants,
        int totalReussis,
        double tauxReussite,
        boolean sessionCloturee
) {
}
