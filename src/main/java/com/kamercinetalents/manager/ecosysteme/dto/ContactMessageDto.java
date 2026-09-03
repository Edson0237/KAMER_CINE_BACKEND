package com.kamercinetalents.manager.ecosysteme.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO immuable pour un message de contact.
 */
public record ContactMessageDto(
        UUID id,
        String nom,
        String email,
        String sujet,
        String message,
        String statut,
        OffsetDateTime dateReception,
        OffsetDateTime dateTraitement
) {}
