package com.kamercinetalents.manager.admin.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entité JPA représentant un paramètre système configurable.
 *
 * <p>Les paramètres système permettent d'ajuster le comportement de
 * l'application (seuils d'alerte, langue par défaut, format SMS, etc.)
 * sans redéploiement de code. Chaque paramètre peut être restreint à
 * un rôle autorisé à le modifier ({@code modifiable_par_role_id}).</p>
 */
@Entity
@Table(name = "parametre_systeme")
public class ParametreSystemeEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "cle", nullable = false, unique = true)
    private String cle;

    @Column(name = "valeur")
    private String valeur;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "description")
    private String description;

    @Column(name = "modifiable_par_role_id")
    private UUID modifiableParRoleId;

    protected ParametreSystemeEntity() {
    }

    public UUID getId() { return id; }
    public String getCle() { return cle; }
    public String getValeur() { return valeur; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public UUID getModifiableParRoleId() { return modifiableParRoleId; }

    public void setValeur(String valeur) { this.valeur = valeur; }
}
