package com.kamercinetalents.manager.pilotage.dto;

/**
 * Indicateur clé du tableau de bord consolidé (M4).
 *
 * @param label le libellé de l'indicateur
 * @param valeur la valeur numérique
 * @param unite l'unité de mesure
 */
public record IndicateurDto(
        String label,
        long valeur,
        String unite
) {
}
