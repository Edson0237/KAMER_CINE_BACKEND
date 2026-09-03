package com.kamercinetalents.manager.iam.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entité JPA représentant un rôle dans le système RBAC.
 *
 * <p>Les rôles sont stockés en table de référence (jamais d'ENUM en base).
 * <p>Le {@code niveauHierarchique} (0 à 7) correspond aux niveaux du système :
 * 0 = Administrateur Système (hors périmètre territorial),
 * N1 Comité Central, N2 Région, N3 Département, N4 Arrondissement,
 * N5 Commune, N6 Encadreur, N7 Apprenant.</p>
 *
 * <p>Ajouter un rôle = un INSERT dans cette table, jamais une migration
 * de schéma ni du code.</p>
 */
@Entity
@Table(name = "role")
public class RoleEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "libelle", nullable = false)
    private String libelle;

    @Column(name = "niveau_hierarchique", nullable = false)
    private short niveauHierarchique;

    public RoleEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public short getNiveauHierarchique() { return niveauHierarchique; }
    public void setNiveauHierarchique(short niveauHierarchique) { this.niveauHierarchique = niveauHierarchique; }
}
