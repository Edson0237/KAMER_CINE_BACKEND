package com.kamercinetalents.manager.sync.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "sync_conflict_log")
public class SyncConflictLogEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "sync_queue_id", nullable = false)
    private UUID syncQueueId;

    @Column(name = "resolution", nullable = false)
    private String resolution;

    @Column(name = "date_resolution", nullable = false)
    private OffsetDateTime dateResolution;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "version_serveur", columnDefinition = "jsonb")
    private Map<String, Object> versionServeur;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "version_client", columnDefinition = "jsonb")
    private Map<String, Object> versionClient;

    @Column(name = "resolu_par_id")
    private UUID resoluParId;

    public SyncConflictLogEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSyncQueueId() { return syncQueueId; }
    public void setSyncQueueId(UUID syncQueueId) { this.syncQueueId = syncQueueId; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public OffsetDateTime getDateResolution() { return dateResolution; }
    public void setDateResolution(OffsetDateTime dateResolution) { this.dateResolution = dateResolution; }
    public Map<String, Object> getVersionServeur() { return versionServeur; }
    public void setVersionServeur(Map<String, Object> versionServeur) { this.versionServeur = versionServeur; }
    public Map<String, Object> getVersionClient() { return versionClient; }
    public void setVersionClient(Map<String, Object> versionClient) { this.versionClient = versionClient; }
    public UUID getResoluParId() { return resoluParId; }
    public void setResoluParId(UUID resoluParId) { this.resoluParId = resoluParId; }
}
