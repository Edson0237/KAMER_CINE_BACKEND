package com.kamercinetalents.manager.iam.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entité JPA représentant une permission dans le système RBAC.
 *
 * <p>Les permissions sont granulaires et indépendantes des rôles.
 * Elles sont associées aux rôles via la table de liaison
 * {@code role_permission}.</p>
 */
@Entity
@Table(name = "permission")
public class PermissionEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "libelle", nullable = false)
    private String libelle;

    public PermissionEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}
