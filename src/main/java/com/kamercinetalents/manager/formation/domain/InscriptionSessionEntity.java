package com.kamercinetalents.manager.formation.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entité JPA représentant l'inscription d'un apprenant à une session — table de liaison N-N.
 */
@Entity
@Table(name = "inscription_session")
public class InscriptionSessionEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "apprenant_id", nullable = false)
    private UUID apprenantId;

    public InscriptionSessionEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }
    public UUID getApprenantId() { return apprenantId; }
    public void setApprenantId(UUID apprenantId) { this.apprenantId = apprenantId; }
}
