package com.kamercinetalents.manager.common.service;

import tools.jackson.databind.ObjectMapper;
import com.kamercinetalents.manager.common.security.SecurityUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Implémentation de {@link AuditService} utilisant JdbcTemplate pour
 * insérer directement dans la table {@code audit_log}.
 *
 * <p>L'insertion se fait dans la même transaction que l'opération métier
 * (grâce au contexte transactionnel Spring), garantissant ainsi l'atomicité
 * de l'opération et de sa trace d'audit (règle ACID).</p>
 */
@Service
public class AuditServiceImpl implements AuditService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Construit le service d'audit avec ses dépendances injectées.
     *
     * @param jdbcTemplate  le template JDBC pour l'insertion
     * @param objectMapper   le mappeur JSON pour sérialiser les détails
     */
    public AuditServiceImpl(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void log(String action, String entiteType, UUID entiteId, Map<String, Object> details) {
        UUID userId = null;
        try {
            userId = SecurityUtils.getCurrentUserId();
        } catch (IllegalStateException ignored) {
            // Opération système sans utilisateur authentifié — userId reste null
        }

        String detailsJson = null;
        if (details != null && !details.isEmpty()) {
            try {
                detailsJson = objectMapper.writeValueAsString(details);
            } catch (Exception e) {
                detailsJson = "{\"error\": \"serialization_failed\"}";
            }
        }

        jdbcTemplate.update(
                "INSERT INTO audit_log (id, utilisateur_id, action, entite_type, entite_id, date, details) " +
                "VALUES (gen_random_uuid(), ?, ?, ?, ?, now(), ?::jsonb)",
                userId, action, entiteType, entiteId, detailsJson
        );
    }
}
