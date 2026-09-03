package com.kamercinetalents.manager.ecosysteme.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entité JPA représentant un message de contact envoyé depuis le site public.
 *
 * <p>Le statut passe de {@code non_traite} à {@code traite} quand un
 * administrateur le consulte.</p>
 */
@Entity
@Table(name = "contact_message")
public class ContactMessageEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "sujet", nullable = false)
    private String sujet;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "statut", nullable = false)
    private String statut;

    @Column(name = "date_reception", nullable = false)
    private OffsetDateTime dateReception;

    @Column(name = "date_traitement")
    private OffsetDateTime dateTraitement;

    public ContactMessageEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSujet() { return sujet; }
    public void setSujet(String sujet) { this.sujet = sujet; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public OffsetDateTime getDateReception() { return dateReception; }
    public void setDateReception(OffsetDateTime dateReception) { this.dateReception = dateReception; }
    public OffsetDateTime getDateTraitement() { return dateTraitement; }
    public void setDateTraitement(OffsetDateTime dateTraitement) { this.dateTraitement = dateTraitement; }
}
