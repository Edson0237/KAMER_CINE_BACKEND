package com.kamercinetalents.manager.formation.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entité JPA représentant un encadreur (formateur) — module M3.
 *
 * <p>L'encadreur est rattaché à un territoire et possède des colonnes
 * de synchronisation. Il anime les sessions de formation et saisit
 * les présences et résultats.</p>
 */
@Entity
@Table(name = "encadreur")
public class EncadreurEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "territoire_id", nullable = false)
    private UUID territoireId;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "specialite")
    private String specialite;

    @Column(name = "disponibilite")
    private String disponibilite;

    @Column(name = "evaluation_moyenne")
    private BigDecimal evaluationMoyenne;

    @Column(name = "photo_url")
    private String photoUrl;

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

    public EncadreurEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTerritoireId() { return territoireId; }
    public void setTerritoireId(UUID territoireId) { this.territoireId = territoireId; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }
    public String getDisponibilite() { return disponibilite; }
    public void setDisponibilite(String disponibilite) { this.disponibilite = disponibilite; }
    public BigDecimal getEvaluationMoyenne() { return evaluationMoyenne; }
    public void setEvaluationMoyenne(BigDecimal evaluationMoyenne) { this.evaluationMoyenne = evaluationMoyenne; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
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
