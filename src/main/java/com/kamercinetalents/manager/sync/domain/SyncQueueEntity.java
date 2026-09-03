package com.kamercinetalents.manager.sync.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "sync_queue")
public class SyncQueueEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "utilisateur_id", nullable = false)
    private UUID utilisateurId;

    @Column(name = "entite_type", nullable = false)
    private String entiteType;

    @Column(name = "entite_id", nullable = false)
    private UUID entiteId;

    @Column(name = "operation", nullable = false)
    private String operation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Column(name = "horodatage_client", nullable = false)
    private OffsetDateTime horodatageClient;

    @Column(name = "horodatage_reception")
    private OffsetDateTime horodatageReception;

    @Column(name = "statut", nullable = false)
    private String statut = "pending";

    @Column(name = "tentative", nullable = false)
    private short tentative = 0;

    @Column(name = "message_erreur")
    private String messageErreur;

    public SyncQueueEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(UUID utilisateurId) { this.utilisateurId = utilisateurId; }
    public String getEntiteType() { return entiteType; }
    public void setEntiteType(String entiteType) { this.entiteType = entiteType; }
    public UUID getEntiteId() { return entiteId; }
    public void setEntiteId(UUID entiteId) { this.entiteId = entiteId; }
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
    public OffsetDateTime getHorodatageClient() { return horodatageClient; }
    public void setHorodatageClient(OffsetDateTime horodatageClient) { this.horodatageClient = horodatageClient; }
    public OffsetDateTime getHorodatageReception() { return horodatageReception; }
    public void setHorodatageReception(OffsetDateTime horodatageReception) { this.horodatageReception = horodatageReception; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public short getTentative() { return tentative; }
    public void setTentative(short tentative) { this.tentative = tentative; }
    public String getMessageErreur() { return messageErreur; }
    public void setMessageErreur(String messageErreur) { this.messageErreur = messageErreur; }
}
