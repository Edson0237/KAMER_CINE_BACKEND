package com.kamercinetalents.manager.ecosysteme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Requête de création ou modification d'un événement.
 */
public record CreateEvenementRequest(
        @NotBlank String titre,
        String description,
        String type,
        @NotNull OffsetDateTime dateDebut,
        OffsetDateTime dateFin,
        String lieu,
        String adresse,
        UUID communeId,
        String imageUrl,
        Integer capacite,
        String statut
) {}
