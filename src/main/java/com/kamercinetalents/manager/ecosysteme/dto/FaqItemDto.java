package com.kamercinetalents.manager.ecosysteme.dto;

import java.util.UUID;

/**
 * DTO immuable pour une question fréquente (FAQ).
 */
public record FaqItemDto(
        UUID id,
        String question,
        String reponse,
        String categorie,
        int ordre,
        boolean actif
) {}
