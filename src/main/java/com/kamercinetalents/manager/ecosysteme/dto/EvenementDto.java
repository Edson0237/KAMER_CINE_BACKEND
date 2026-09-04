package com.kamercinetalents.manager.ecosysteme.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO immuable pour un événement.
 */
public record EvenementDto(
        UUID id,
        String titre,
        String description,
        String type,
        OffsetDateTime dateDebut,
        OffsetDateTime dateFin,
        String lieu,
        String adresse,
        UUID communeId,
        String imageUrl,
        Integer capacite,
        String statut
) {}
