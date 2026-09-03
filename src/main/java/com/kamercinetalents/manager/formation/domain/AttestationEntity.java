package com.kamercinetalents.manager.formation.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entité JPA représentant une attestation — module M3.
 *
 * <p>Générée côté serveur uniquement, jamais hors-ligne. Le numéro unique
 * est attribué à la création.</p>
 */
@Entity
@Table(name = "attestation")
public class AttestationEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "apprenant_id", nullable = false)
    private UUID apprenantId;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "numero", nullable = false, unique = true)
    private String numero;

    @Column(name = "date_delivrance", nullable = false)
    private LocalDate dateDelivrance;

    @Column(name = "fichier_url")
    private String fichierUrl;

    public AttestationEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getApprenantId() { return apprenantId; }
    public void setApprenantId(UUID apprenantId) { this.apprenantId = apprenantId; }
    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public LocalDate getDateDelivrance() { return dateDelivrance; }
    public void setDateDelivrance(LocalDate dateDelivrance) { this.dateDelivrance = dateDelivrance; }
    public String getFichierUrl() { return fichierUrl; }
    public void setFichierUrl(String fichierUrl) { this.fichierUrl = fichierUrl; }
}
