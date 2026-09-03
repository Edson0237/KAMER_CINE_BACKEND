package com.kamercinetalents.manager.ecosysteme.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO immuable pour une candidature publique.
 */
public record CandidaturePubliqueDto(
        UUID id,
        String nom,
        String prenom,
        String email,
        String telephone,
        String motivation,
        String statut,
        OffsetDateTime dateSoumission,
        OffsetDateTime dateTraitement,
        UUID communeId,
        UUID traitePar
) {}
