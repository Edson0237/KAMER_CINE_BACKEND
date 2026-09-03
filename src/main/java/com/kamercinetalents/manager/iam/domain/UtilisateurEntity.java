package com.kamercinetalents.manager.iam.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entité JPA représentant un utilisateur du système KAMER CINÉ TALENTS MANAGER.
 *
 * <p>Chaque utilisateur est rattaché à un rôle (RBAC) et à un territoire
 * qui détermine son périmètre d'accès aux données (7 niveaux hiérarchiques).
 * Cette entité n'est jamais exposée directement à l'API ; les DTOs
 * {@code UtilisateurDto} servent de contrat d'échange.</p>
 *
 * <p><strong>Colonnes de synchronisation :</strong> {@code serverUpdatedAt},
 * {@code clientUpdatedAt}, {@code syncStatus} — obligatoires car la table
 * est modifiable sur le terrain via l'application mobile.</p>
 */
@Entity
@Table(name = "utilisateur")
public class UtilisateurEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Column(name = "territoire_id")
    private UUID territoireId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "server_updated_at")
    private OffsetDateTime serverUpdatedAt;

    @Column(name = "client_updated_at")
    private OffsetDateTime clientUpdatedAt;

    @Column(name = "sync_status")
    private String syncStatus;

    /**
     * Constructeur par défaut requis par JPA.
     */
    public UtilisateurEntity() {
    }

    /**
     * Construit un utilisateur avec les champs obligatoires.
     *
     * @param id           l'UUID généré (côté client si hors-ligne, sinon serveur)
     * @param nom          le nom complet de l'utilisateur
     * @param email        l'adresse email unique
     * @param passwordHash le hash BCrypt du mot de passe
     * @param roleId       l'UUID du rôle assigné
     * @param territoireId l'UUID du territoire de périmètre
     */
    public UtilisateurEntity(UUID id, String nom, String email, String passwordHash,
                             UUID roleId, UUID territoireId) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roleId = roleId;
        this.territoireId = territoireId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public UUID getRoleId() { return roleId; }
    public void setRoleId(UUID roleId) { this.roleId = roleId; }
    public UUID getTerritoireId() { return territoireId; }
    public void setTerritoireId(UUID territoireId) { this.territoireId = territoireId; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public OffsetDateTime getServerUpdatedAt() { return serverUpdatedAt; }
    public void setServerUpdatedAt(OffsetDateTime serverUpdatedAt) { this.serverUpdatedAt = serverUpdatedAt; }
    public OffsetDateTime getClientUpdatedAt() { return clientUpdatedAt; }
    public void setClientUpdatedAt(OffsetDateTime clientUpdatedAt) { this.clientUpdatedAt = clientUpdatedAt; }
    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
}
