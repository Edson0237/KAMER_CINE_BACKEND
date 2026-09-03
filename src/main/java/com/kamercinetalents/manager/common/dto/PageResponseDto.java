package com.kamercinetalents.manager.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Enveloppe générique de réponse paginée pour les listes principales
 * (apprenants, encadreurs, sessions, utilisateurs, communes).
 *
 * <p>Uniformise le contrat d'échange entre l'API et les clients (web/mobile)
 * pour toute liste susceptible de dépasser quelques dizaines d'éléments
 * (ex. 360 communes à terme). Le frontend pilote la pagination via les
 * paramètres de requête {@code page} (0-indexé) et {@code size}.</p>
 *
 * @param content        les éléments de la page courante
 * @param page           le numéro de page courant (0-indexé)
 * @param size           la taille de page demandée
 * @param totalElements  le nombre total d'éléments toutes pages confondues
 * @param totalPages     le nombre total de pages
 */
public record PageResponseDto<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    /**
     * Construit un {@link PageResponseDto} à partir d'une {@link Page} Spring Data
     * en appliquant une fonction de conversion entité → DTO.
     *
     * @param springPage la page Spring Data source
     * @param mapper     la fonction de conversion élément → DTO
     * @param <E>        le type source (entité)
     * @param <T>        le type cible (DTO)
     * @return le DTO de page prêt à être sérialisé
     */
    public static <E, T> PageResponseDto<T> from(Page<E> springPage, Function<E, T> mapper) {
        return new PageResponseDto<>(
                springPage.getContent().stream().map(mapper).toList(),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements(),
                springPage.getTotalPages()
        );
    }
}
