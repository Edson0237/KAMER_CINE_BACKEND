package com.kamercinetalents.manager.ecosysteme.dto;

import java.util.UUID;

/**
 * Résultat du traitement d'une candidature acceptée.
 *
 * <p>Contient les identifiants générés pour le compte apprenant :
 * login, mot de passe temporaire, et le canal utilisé pour transmettre
 * les identifiants (sms, email, ou {@code afficher_ecran} si aucun
 * canal fiable n'est disponible).</p>
 */
public record CandidatureAccepteeResult(
        UUID candidatureId,
        UUID apprenantId,
        UUID utilisateurId,
        String login,
        String motDePasseTemporaire,
        String canalTransmission
) {}
