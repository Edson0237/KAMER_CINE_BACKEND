package com.kamercinetalents.manager.notification.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "template_notification")
public class TemplateNotificationEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "canal", nullable = false)
    private String canal;

    @Column(name = "sujet", nullable = false)
    private String sujet;

    @Column(name = "corps", nullable = false)
    private String corps;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "variables", columnDefinition = "jsonb")
    private Map<String, Object> variables;

    @Column(name = "langue", nullable = false)
    private String langue = "fr";

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    public TemplateNotificationEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }
    public String getSujet() { return sujet; }
    public void setSujet(String sujet) { this.sujet = sujet; }
    public String getCorps() { return corps; }
    public void setCorps(String corps) { this.corps = corps; }
    public Map<String, Object> getVariables() { return variables; }
    public void setVariables(Map<String, Object> variables) { this.variables = variables; }
    public String getLangue() { return langue; }
    public void setLangue(String langue) { this.langue = langue; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}
