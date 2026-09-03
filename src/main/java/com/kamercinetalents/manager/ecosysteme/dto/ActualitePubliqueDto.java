package com.kamercinetalents.manager.ecosysteme.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO immuable pour une actualité publique.
 *
 * @param id             l'identifiant unique
 * @param titre          le titre de l'article
 * @param contenu        le corps de l'article
 * @param imageUrl       l'URL de l'image (optionnelle)
 * @param datePublication la date de publication
 * @param statut         le statut ({@code brouillon} ou {@code publiee})
 */
public record ActualitePubliqueDto(
        UUID id,
        String titre,
        String contenu,
        String imageUrl,
        OffsetDateTime datePublication,
        String statut
) {}
