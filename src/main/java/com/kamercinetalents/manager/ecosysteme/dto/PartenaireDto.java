package com.kamercinetalents.manager.ecosysteme.dto;

import java.util.UUID;

/**
 * DTO immuable pour un partenaire.
 */
public record PartenaireDto(
        UUID id,
        String nom,
        String logoUrl,
        String siteWeb,
        int ordre
) {}
