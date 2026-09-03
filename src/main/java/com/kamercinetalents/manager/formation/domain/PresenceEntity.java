package com.kamercinetalents.manager.formation.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entité JPA représentant une présence — table la plus sollicitée hors-ligne.
 *
 * <p>Saisie par un utilisateur (encadreur) sur le terrain, avec colonnes
 * de synchronisation pour le mode hors-ligne (M5).</p>
 */
@Entity
@Table(name = "presence")
public class PresenceEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "apprenant_id", nullable = false)
    private UUID apprenantId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "statut", nullable = false)
    private String statut;

    @Column(name = "saisie_par_id", nullable = false)
    private UUID saisieParId;

    @Column(name = "server_updated_at")
    private OffsetDateTime serverUpdatedAt;

    @Column(name = "client_updated_at")
    private OffsetDateTime clientUpdatedAt;

    @Column(name = "sync_status")
    private String syncStatus;

    public PresenceEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }
    public UUID getApprenantId() { return apprenantId; }
    public void setApprenantId(UUID apprenantId) { this.apprenantId = apprenantId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public UUID getSaisieParId() { return saisieParId; }
    public void setSaisieParId(UUID saisieParId) { this.saisieParId = saisieParId; }
    public OffsetDateTime getServerUpdatedAt() { return serverUpdatedAt; }
    public void setServerUpdatedAt(OffsetDateTime serverUpdatedAt) { this.serverUpdatedAt = serverUpdatedAt; }
    public OffsetDateTime getClientUpdatedAt() { return clientUpdatedAt; }
    public void setClientUpdatedAt(OffsetDateTime clientUpdatedAt) { this.clientUpdatedAt = clientUpdatedAt; }
    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
}
