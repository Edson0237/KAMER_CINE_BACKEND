package com.kamercinetalents.manager.admin.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entité JPA représentant une ligne du journal d'audit polymorphe.
 *
 * <p>Le journal d'audit est unique pour tous les modules (M0 à M13) :
 * {@code entite_type} + {@code entite_id} identifient l'entité concernée
 * de façon générique, sans nécessiter une table de log par module.</p>
 *
 * <p>Cette entité est en lecture seule depuis l'API — aucune modification
 * ou suppression n'est permise. L'insertion se fait exclusivement via
 * {@link com.kamercinetalents.manager.common.service.AuditService}.</p>
 */
@Entity
@Table(name = "audit_log")
public class AuditLogEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "utilisateur_id")
    private UUID utilisateurId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "entite_type", nullable = false)
    private String entiteType;

    @Column(name = "entite_id", nullable = false)
    private UUID entiteId;

    @Column(name = "date", nullable = false)
    private OffsetDateTime date;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = "jsonb")
    private Map<String, Object> details;

    protected AuditLogEntity() {
    }

    public UUID getId() { return id; }
    public UUID getUtilisateurId() { return utilisateurId; }
    public String getAction() { return action; }
    public String getEntiteType() { return entiteType; }
    public UUID getEntiteId() { return entiteId; }
    public OffsetDateTime getDate() { return date; }
    public Map<String, Object> getDetails() { return details; }
}
