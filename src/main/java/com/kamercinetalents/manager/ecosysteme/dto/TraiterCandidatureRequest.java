package com.kamercinetalents.manager.ecosysteme.dto;

import java.util.UUID;

/**
 * Requête de traitement d'une candidature (acceptation ou refus).
 *
 * @param statut    le nouveau statut : {@code acceptee} ou {@code refusee}
 * @param communeId l'UUID de la commune de rattachement (obligatoire si acceptee)
 */
public record TraiterCandidatureRequest(
        String statut,
        UUID communeId
) {}
