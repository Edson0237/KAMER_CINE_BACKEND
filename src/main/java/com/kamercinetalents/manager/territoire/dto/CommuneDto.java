package com.kamercinetalents.manager.territoire.dto;

import java.util.UUID;

/**
 * DTO de consultation d'une commune avec compteurs de déploiement.
 *
 * @param id                l'identifiant unique de la commune
 * @param nom               le nom de la commune
 * @param territoireId      l'UUID du territoire (commune)
 * @param statutCommune     le code du statut (terminee, en_cours, non_demarree, suspendue)
 * @param nombreApprenants  nombre d'apprenants dans la commune
 * @param nombreEncadreurs  nombre d'encadreurs dans la commune
 * @param nombreSessions    nombre de sessions de formation dans la commune
 */
public record CommuneDto(
        UUID id,
        String nom,
        UUID territoireId,
        String statutCommune,
        long nombreApprenants,
        long nombreEncadreurs,
        long nombreSessions
) {
}
