package com.kamercinetalents.manager.formation.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entité JPA représentant une session de formation — module M3.
 *
 * <p>Une session est rattachée à un territoire et animée par un encadreur.
 * Elle possède des colonnes de synchronisation car elle peut être créée
 * ou modifiée sur le terrain.</p>
 */
@Entity
@Table(name = "session_formation")
public class SessionFormationEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "territoire_id", nullable = false)
    private UUID territoireId;

    @Column(name = "encadreur_id", nullable = false)
    private UUID encadreurId;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "lieu")
    private String lieu;

    @Column(name = "programme")
    private String programme;

    @Column(name = "statut", nullable = false)
    private String statut = "planifiee";

    @Column(name = "server_updated_at")
    private OffsetDateTime serverUpdatedAt;

    @Column(name = "client_updated_at")
    private OffsetDateTime clientUpdatedAt;

    @Column(name = "sync_status")
    private String syncStatus;

    public SessionFormationEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTerritoireId() { return territoireId; }
    public void setTerritoireId(UUID territoireId) { this.territoireId = territoireId; }
    public UUID getEncadreurId() { return encadreurId; }
    public void setEncadreurId(UUID encadreurId) { this.encadreurId = encadreurId; }
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    public String getProgramme() { return programme; }
    public void setProgramme(String programme) { this.programme = programme; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public OffsetDateTime getServerUpdatedAt() { return serverUpdatedAt; }
    public void setServerUpdatedAt(OffsetDateTime serverUpdatedAt) { this.serverUpdatedAt = serverUpdatedAt; }
    public OffsetDateTime getClientUpdatedAt() { return clientUpdatedAt; }
    public void setClientUpdatedAt(OffsetDateTime clientUpdatedAt) { this.clientUpdatedAt = clientUpdatedAt; }
    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
}
