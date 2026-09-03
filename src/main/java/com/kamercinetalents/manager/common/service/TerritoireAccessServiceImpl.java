package com.kamercinetalents.manager.common.service;

import com.kamercinetalents.manager.common.exception.PerimeterAccessException;
import com.kamercinetalents.manager.common.security.SecurityContext;
import com.kamercinetalents.manager.common.security.SecurityUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implémentation de {@link TerritoireAccessService} basée sur une CTE récursive
 * PostgreSQL.
 *
 * <p>La vérification se fait en SQL avec une expression de table commune
 * récursive (WITH RECURSIVE) qui remonte la hiérarchie des territoires
 * depuis le territoire demandé jusqu'à la racine. Si le territoire de
 * l'utilisateur apparaît dans ce chemin, l'accès est accordé.</p>
 *
 * <p>Le N1 (niveau hiérarchique = 1) a un accès global — la vérification
 * est court-circuitée pour éviter une requête inutile.</p>
 */
@Service
public class TerritoireAccessServiceImpl implements TerritoireAccessService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Construit le service avec le template JDBC injecté.
     *
     * @param jdbcTemplate le template JDBC pour les requêtes natives
     */
    public TerritoireAccessServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean canAccess(UUID territoireId) {
        SecurityContext ctx = SecurityUtils.get();

        // N1 (Comité Central) — accès global
        if (ctx.niveauHierarchique() == 1) {
            return true;
        }

        // Si l'utilisateur n'a pas de territoire, refuser
        UUID userTerritoireId = ctx.territoireId();
        if (userTerritoireId == null) {
            return false;
        }

        // Le territoire demandé est exactement celui de l'utilisateur
        if (userTerritoireId.equals(territoireId)) {
            return true;
        }

        // CTE récursive : remonte depuis le territoire demandé jusqu'à la racine.
        // Si le territoire de l'utilisateur apparaît dans la chaîne, l'accès est accordé.
        String sql = """
                WITH RECURSIVE hierarchy AS (
                    SELECT id, parent_id FROM territoire WHERE id = ?
                    UNION ALL
                    SELECT t.id, t.parent_id FROM territoire t
                    JOIN hierarchy h ON t.id = h.parent_id
                )
                SELECT COUNT(*) FROM hierarchy WHERE id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, territoireId, userTerritoireId);
        return count != null && count > 0;
    }

    @Override
    public void requireAccess(UUID territoireId) {
        if (!canAccess(territoireId)) {
            throw new PerimeterAccessException(
                    "Accès refusé : le territoire " + territoireId +
                    " n'est pas dans le périmètre de l'utilisateur courant");
        }
    }

    @Override
    public UUID getCurrentPerimeter() {
        return SecurityUtils.getCurrentTerritoireId();
    }
}
