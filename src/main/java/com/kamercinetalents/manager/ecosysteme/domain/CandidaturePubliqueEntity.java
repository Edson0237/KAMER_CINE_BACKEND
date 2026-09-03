package com.kamercinetalents.manager.ecosysteme.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entité JPA représentant une candidature publique au programme.
 *
 * <p>Le statut évolue : {@code en_attente} → {@code acceptee} ou
 * {@code refusee}. Quand une candidature est acceptée, un compte
 * apprenant (N7) est créé avec un mot de passe temporaire.</p>
 */
@Entity
@Table(name = "candidature_publique")
public class CandidaturePubliqueEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "motivation")
    private String motivation;

    @Column(name = "statut", nullable = false)
    private String statut;

    @Column(name = "date_soumission", nullable = false)
    private OffsetDateTime dateSoumission;

    @Column(name = "date_traitement")
    private OffsetDateTime dateTraitement;

    @Column(name = "commune_id")
    private UUID communeId;

    @Column(name = "traite_par")
    private UUID traitePar;

    public CandidaturePubliqueEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getMotivation() { return motivation; }
    public void setMotivation(String motivation) { this.motivation = motivation; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public OffsetDateTime getDateSoumission() { return dateSoumission; }
    public void setDateSoumission(OffsetDateTime dateSoumission) { this.dateSoumission = dateSoumission; }
    public OffsetDateTime getDateTraitement() { return dateTraitement; }
    public void setDateTraitement(OffsetDateTime dateTraitement) { this.dateTraitement = dateTraitement; }
    public UUID getCommuneId() { return communeId; }
    public void setCommuneId(UUID communeId) { this.communeId = communeId; }
    public UUID getTraitePar() { return traitePar; }
    public void setTraitePar(UUID traitePar) { this.traitePar = traitePar; }
}
