package com.kamercinetalents.manager.notification.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "notification")
public class NotificationEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "template_id", nullable = false)
    private UUID templateId;

    @Column(name = "utilisateur_id", nullable = false)
    private UUID utilisateurId;

    @Column(name = "canal", nullable = false)
    private String canal;

    @Column(name = "contenu_final", nullable = false)
    private String contenuFinal;

    @Column(name = "statut", nullable = false)
    private String statut = "en_attente";

    @Column(name = "date_envoi")
    private OffsetDateTime dateEnvoi;

    @Column(name = "date_lecture")
    private OffsetDateTime dateLecture;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    public NotificationEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID templateId) { this.templateId = templateId; }
    public UUID getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(UUID utilisateurId) { this.utilisateurId = utilisateurId; }
    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }
    public String getContenuFinal() { return contenuFinal; }
    public void setContenuFinal(String contenuFinal) { this.contenuFinal = contenuFinal; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public OffsetDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(OffsetDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }
    public OffsetDateTime getDateLecture() { return dateLecture; }
    public void setDateLecture(OffsetDateTime dateLecture) { this.dateLecture = dateLecture; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
