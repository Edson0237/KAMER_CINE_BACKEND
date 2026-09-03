package com.kamercinetalents.manager.formation.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entité JPA représentant un apprenant — fiche principale du module M3.
 *
 * <p>L'apprenant est rattaché à un territoire (commune) et possède des
 * colonnes de synchronisation car il est modifiable sur le terrain.
 * La suppression douce ({@code deletedAt}) protège contre les suppressions
 * accidentelles hors-ligne.</p>
 */
@Entity
@Table(name = "apprenant")
public class ApprenantEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "territoire_id", nullable = false)
    private UUID territoireId;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "sexe")
    private String sexe;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "photo_url")
    private String photoUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "competences", columnDefinition = "jsonb")
    private Map<String, Object> competences;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "portfolio", columnDefinition = "jsonb")
    private Map<String, Object> portfolio;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "server_updated_at")
    private OffsetDateTime serverUpdatedAt;

    @Column(name = "client_updated_at")
    private OffsetDateTime clientUpdatedAt;

    @Column(name = "sync_status")
    private String syncStatus;

    public ApprenantEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTerritoireId() { return territoireId; }
    public void setTerritoireId(UUID territoireId) { this.territoireId = territoireId; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }
    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public Map<String, Object> getCompetences() { return competences; }
    public void setCompetences(Map<String, Object> competences) { this.competences = competences; }
    public Map<String, Object> getPortfolio() { return portfolio; }
    public void setPortfolio(Map<String, Object> portfolio) { this.portfolio = portfolio; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt; }
    public OffsetDateTime getServerUpdatedAt() { return serverUpdatedAt; }
    public void setServerUpdatedAt(OffsetDateTime serverUpdatedAt) { this.serverUpdatedAt = serverUpdatedAt; }
    public OffsetDateTime getClientUpdatedAt() { return clientUpdatedAt; }
    public void setClientUpdatedAt(OffsetDateTime clientUpdatedAt) { this.clientUpdatedAt = clientUpdatedAt; }
    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
}
