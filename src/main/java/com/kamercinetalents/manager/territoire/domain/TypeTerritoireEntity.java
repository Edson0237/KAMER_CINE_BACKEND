package com.kamercinetalents.manager.territoire.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entité JPA représentant un type de territoire (table de référence).
 *
 * <p>Les types de territoire sont stockés en base (jamais d'ENUM PostgreSQL) :
 * Pays, Région, Département, Arrondissement, Commune, etc.</p>
 */
@Entity
@Table(name = "type_territoire")
public class TypeTerritoireEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "libelle", nullable = false)
    private String libelle;

    @Column(name = "niveau", nullable = false)
    private short niveau;

    protected TypeTerritoireEntity() {
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getLibelle() { return libelle; }
    public short getNiveau() { return niveau; }
}
