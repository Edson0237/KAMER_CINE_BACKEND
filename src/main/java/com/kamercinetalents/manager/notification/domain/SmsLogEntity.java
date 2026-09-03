package com.kamercinetalents.manager.notification.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "sms_log")
public class SmsLogEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "notification_id", nullable = false)
    private UUID notificationId;

    @Column(name = "numero_destinataire", nullable = false)
    private String numeroDestinataire;

    @Column(name = "fournisseur", nullable = false)
    private String fournisseur = "orange";

    @Column(name = "statut_fournisseur")
    private String statutFournisseur;

    @Column(name = "cout")
    private BigDecimal cout;

    @Column(name = "date_envoi", nullable = false)
    private OffsetDateTime dateEnvoi;

    @Column(name = "tentative", nullable = false)
    private short tentative = 0;

    public SmsLogEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getNotificationId() { return notificationId; }
    public void setNotificationId(UUID notificationId) { this.notificationId = notificationId; }
    public String getNumeroDestinataire() { return numeroDestinataire; }
    public void setNumeroDestinataire(String numeroDestinataire) { this.numeroDestinataire = numeroDestinataire; }
    public String getFournisseur() { return fournisseur; }
    public void setFournisseur(String fournisseur) { this.fournisseur = fournisseur; }
    public String getStatutFournisseur() { return statutFournisseur; }
    public void setStatutFournisseur(String statutFournisseur) { this.statutFournisseur = statutFournisseur; }
    public BigDecimal getCout() { return cout; }
    public void setCout(BigDecimal cout) { this.cout = cout; }
    public OffsetDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(OffsetDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }
    public short getTentative() { return tentative; }
    public void setTentative(short tentative) { this.tentative = tentative; }
}
