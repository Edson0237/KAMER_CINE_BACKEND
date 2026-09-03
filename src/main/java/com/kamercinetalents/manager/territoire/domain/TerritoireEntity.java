package com.kamercinetalents.manager.territoire.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entité JPA représentant un territoire — entité générique auto-référentielle.
 *
 * <p>Le territoire est l'entité centrale du système : il représente
 * n'importe quel niveau de la hiérarchie administrative camerounaise
 * (Pays, Région, Département, Arrondissement, Commune) grâce à la
 * colonne {@code type_territoire_id} et à l'auto-référence
 * {@code parent_id}.</p>
 *
 * <p>L'auto-référence permet de construire une arborescence :
 * le Pays est parent des Régions, qui sont parentes des Départements,
 * etc. La vérification du périmètre territorial utilise une CTE
 * récursive sur cette hiérarchie.</p>
 *
 * <p><strong>Colonnes de synchronisation :</strong> {@code serverUpdatedAt},
 * {@code clientUpdatedAt}, {@code syncStatus} — obligatoires car la table
 * est modifiable sur le terrain.</p>
 *
 * <p><strong>Suppression douce :</strong> {@code deletedAt} — un territoire
 * n'est jamais supprimé physiquement, seulement marqué inactif.</p>
 */
@Entity
@Table(name = "territoire")
public class TerritoireEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "type_territoire_id", nullable = false)
    private UUID typeTerritoireId;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "statut_commune_id")
    private UUID statutCommuneId;

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

    public TerritoireEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public UUID getTypeTerritoireId() { return typeTerritoireId; }
    public void setTypeTerritoireId(UUID typeTerritoireId) { this.typeTerritoireId = typeTerritoireId; }
    public UUID getParentId() { return parentId; }
    public void setParentId(UUID parentId) { this.parentId = parentId; }
    public UUID getStatutCommuneId() { return statutCommuneId; }
    public void setStatutCommuneId(UUID statutCommuneId) { this.statutCommuneId = statutCommuneId; }
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
