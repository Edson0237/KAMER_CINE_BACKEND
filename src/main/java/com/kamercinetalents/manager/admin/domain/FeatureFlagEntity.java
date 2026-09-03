package com.kamercinetalents.manager.admin.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entité JPA représentant un feature flag (activation/désactivation de
 * module par territoire).
 *
 * <p>Permet le rollout progressif d'un module V2/V3/V4 sur des communes
 * pilotes avant généralisation. Si {@code territoire_id} est null, le
 * flag s'applique globalement.</p>
 */
@Entity
@Table(name = "feature_flag")
public class FeatureFlagEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "libelle", nullable = false)
    private String libelle;

    @Column(name = "actif", nullable = false)
    private boolean actif;

    @Column(name = "version_cible")
    private String versionCible;

    @Column(name = "territoire_id")
    private UUID territoireId;

    protected FeatureFlagEntity() {
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getLibelle() { return libelle; }
    public boolean isActif() { return actif; }
    public String getVersionCible() { return versionCible; }
    public UUID getTerritoireId() { return territoireId; }

    public void setActif(boolean actif) { this.actif = actif; }
}
