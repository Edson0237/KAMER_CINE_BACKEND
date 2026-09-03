package com.kamercinetalents.manager.territoire.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entité JPA représentant un statut de commune (table de référence).
 *
 * <p>Les statuts de commune sont stockés en base : Actif, Suspendu,
 * En attente, etc. Jamais d'ENUM en base.</p>
 */
@Entity
@Table(name = "statut_commune")
public class StatutCommuneEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "libelle", nullable = false)
    private String libelle;

    protected StatutCommuneEntity() {
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getLibelle() { return libelle; }
}
