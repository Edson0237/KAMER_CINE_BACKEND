package com.kamercinetalents.manager.ecosysteme.dto;

import java.util.UUID;

/**
 * DTO immuable pour un membre de l'équipe.
 */
public record MembreEquipeDto(
        UUID id,
        String nom,
        String poste,
        String photoUrl,
        String bio,
        int ordre
) {}
