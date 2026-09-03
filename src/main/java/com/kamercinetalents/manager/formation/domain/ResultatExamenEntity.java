package com.kamercinetalents.manager.formation.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entité JPA représentant un résultat d'examen — module M3.
 *
 * <p>Saisi par l'encadreur, avec colonnes de synchronisation pour le mode hors-ligne.</p>
 */
@Entity
@Table(name = "resultat_examen")
public class ResultatExamenEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "apprenant_id", nullable = false)
    private UUID apprenantId;

    @Column(name = "note")
    private BigDecimal note;

    @Column(name = "date_examen", nullable = false)
    private LocalDate dateExamen;

    @Column(name = "server_updated_at")
    private OffsetDateTime serverUpdatedAt;

    @Column(name = "client_updated_at")
    private OffsetDateTime clientUpdatedAt;

    @Column(name = "sync_status")
    private String syncStatus;

    public ResultatExamenEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }
    public UUID getApprenantId() { return apprenantId; }
    public void setApprenantId(UUID apprenantId) { this.apprenantId = apprenantId; }
    public BigDecimal getNote() { return note; }
    public void setNote(BigDecimal note) { this.note = note; }
    public LocalDate getDateExamen() { return dateExamen; }
    public void setDateExamen(LocalDate dateExamen) { this.dateExamen = dateExamen; }
    public OffsetDateTime getServerUpdatedAt() { return serverUpdatedAt; }
    public void setServerUpdatedAt(OffsetDateTime serverUpdatedAt) { this.serverUpdatedAt = serverUpdatedAt; }
    public OffsetDateTime getClientUpdatedAt() { return clientUpdatedAt; }
    public void setClientUpdatedAt(OffsetDateTime clientUpdatedAt) { this.clientUpdatedAt = clientUpdatedAt; }
    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
}
