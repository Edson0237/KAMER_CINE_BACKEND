package com.kamercinetalents.manager.ecosysteme.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entité JPA représentant un membre de l'équipe (Comité Central).
 */
@Entity
@Table(name = "membre_equipe")
public class MembreEquipeEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "poste", nullable = false)
    private String poste;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "bio")
    private String bio;

    @Column(name = "ordre", nullable = false)
    private int ordre;

    public MembreEquipeEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPoste() { return poste; }
    public void setPoste(String poste) { this.poste = poste; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }
}
