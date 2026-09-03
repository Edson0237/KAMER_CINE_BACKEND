package com.kamercinetalents.manager.sync.service;

import com.kamercinetalents.manager.common.service.AuditService;
import com.kamercinetalents.manager.common.service.TerritoireAccessService;
import com.kamercinetalents.manager.formation.domain.*;
import com.kamercinetalents.manager.formation.repository.*;
import com.kamercinetalents.manager.sync.domain.SyncConflictLogEntity;
import com.kamercinetalents.manager.sync.domain.SyncQueueEntity;
import com.kamercinetalents.manager.sync.dto.*;
import com.kamercinetalents.manager.sync.repository.SyncConflictLogRepository;
import com.kamercinetalents.manager.sync.repository.SyncQueueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Processeur d'actions de synchronisation — chaque action s'exécute dans
 * une transaction indépendante ({@code Propagation.REQUIRES_NEW}).
 *
 * <p>Cette classe est séparée de {@link SyncService} pour garantir que
 * l'annotation {@code @Transactional(propagation = REQUIRES_NEW)} soit
 * honorée par le proxy Spring. Un appel direct (self-invocation) dans la
 * même classe contourne le proxy et n'ouvre pas de nouvelle transaction.</p>
 *
 * <p><b>Garantie ACID :</b> si une action échoue, sa transaction est
 * annulée (rollback) sans affecter les autres actions déjà traitées ou
 * à venir. Aucune écriture partielle n'est visible en cas d'erreur.</p>
 */
@Service
public class SyncActionProcessor {

    private final SyncQueueRepository syncQueueRepository;
    private final SyncConflictLogRepository conflictLogRepository;
    private final ApprenantRepository apprenantRepository;
    private final EncadreurRepository encadreurRepository;
    private final SessionFormationRepository sessionRepository;
    private final PresenceRepository presenceRepository;
    private final ResultatExamenRepository resultatRepository;
    private final TerritoireAccessService territoireAccessService;
    private final AuditService auditService;

    public SyncActionProcessor(
            SyncQueueRepository syncQueueRepository,
            SyncConflictLogRepository conflictLogRepository,
            ApprenantRepository apprenantRepository,
            EncadreurRepository encadreurRepository,
            SessionFormationRepository sessionRepository,
            PresenceRepository presenceRepository,
            ResultatExamenRepository resultatRepository,
            TerritoireAccessService territoireAccessService,
            AuditService auditService) {
        this.syncQueueRepository = syncQueueRepository;
        this.conflictLogRepository = conflictLogRepository;
        this.apprenantRepository = apprenantRepository;
        this.encadreurRepository = encadreurRepository;
        this.sessionRepository = sessionRepository;
        this.presenceRepository = presenceRepository;
        this.resultatRepository = resultatRepository;
        this.territoireAccessService = territoireAccessService;
        this.auditService = auditService;
    }

    /**
     * Traite une action individuelle dans une transaction indépendante.
     *
     * <p>Si une exception est levée, la transaction est annulée (rollback)
     * et l'action est marquée comme 'rejected'. Les autres actions du lot
     * ne sont pas affectées.</p>
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SyncResultDto processAction(SyncActionDto action, UUID utilisateurId) {
        try {
            // --- Idempotence : détecter un retry ---
            // L'id de l'action côté mobile devient l'id de l'entrée sync_queue côté serveur.
            // Si une entrée avec le même id existe déjà avec statut='applied' ou 'conflict',
            // c'est un retry (connexion coupée après traitement, avant réception de la réponse).
            // On renvoie directement le statut précédent sans ré-exécuter ni journaliser.
            Optional<SyncQueueEntity> existing = syncQueueRepository.findById(action.id());
            if (existing.isPresent()) {
                SyncQueueEntity prev = existing.get();
                String prevStatut = prev.getStatut();
                if ("applied".equals(prevStatut) || "conflict".equals(prevStatut)) {
                    return new SyncResultDto(action.id(), prevStatut,
                            "conflict".equals(prevStatut) ? "Retry — conflit précédent résolu" : null);
                }
            }

            // Enregistrer la réception dans sync_queue (id = action.id pour idempotence)
            SyncQueueEntity queueEntry = existing.orElseGet(SyncQueueEntity::new);
            queueEntry.setId(action.id());
            queueEntry.setUtilisateurId(utilisateurId);
            queueEntry.setEntiteType(action.entiteType());
            queueEntry.setEntiteId(action.entiteId());
            queueEntry.setOperation(action.operation());
            queueEntry.setPayload(action.payload());
            queueEntry.setHorodatageClient(action.horodatageClient());
            queueEntry.setHorodatageReception(OffsetDateTime.now());
            queueEntry.setStatut("pending");
            syncQueueRepository.save(queueEntry);

            // Dispatcher selon le type d'entité
            SyncResultDto result = switch (action.entiteType()) {
                case "apprenant" -> syncApprenant(action);
                case "encadreur" -> syncEncadreur(action);
                case "session_formation" -> syncSession(action);
                case "presence" -> syncPresence(action, utilisateurId);
                case "resultat_examen" -> syncResultat(action);
                case "attestation" -> new SyncResultDto(action.id(), "rejected",
                        "Attestation non synchronisable via /api/sync — utiliser l'endpoint serveur dédié POST /api/formation/attestations (réservé N1-N5)");
                default -> new SyncResultDto(action.id(), "rejected",
                        "Type d'entité non supporté: " + action.entiteType());
            };

            // Mettre à jour le statut dans sync_queue
            queueEntry.setStatut(result.statut());
            if ("rejected".equals(result.statut())) {
                queueEntry.setMessageErreur(result.message());
            }
            syncQueueRepository.save(queueEntry);

            return result;
        } catch (Exception e) {
            return new SyncResultDto(action.id(), "rejected", e.getMessage());
        }
    }

    // ==================== APPRENANT ====================

    private SyncResultDto syncApprenant(SyncActionDto action) {
        UUID entiteId = action.entiteId();
        Optional<ApprenantEntity> existing = apprenantRepository.findById(entiteId);

        if (existing.isEmpty()) {
            UUID territoireId = toUUID(action.payload().get("territoireId"));
            if (territoireId != null) {
                territoireAccessService.requireAccess(territoireId);
            }
            ApprenantEntity e = new ApprenantEntity();
            e.setId(entiteId);
            mapApprenantPayload(e, action.payload());
            e.setSyncStatus("synced");
            e.setServerUpdatedAt(OffsetDateTime.now());
            e.setClientUpdatedAt(action.horodatageClient());
            apprenantRepository.save(e);
            auditService.log("sync_create", "apprenant", entiteId, Map.of("source", "sync"));
            return new SyncResultDto(action.id(), "applied", null);
        }

        ApprenantEntity current = existing.get();
        territoireAccessService.requireAccess(current.getTerritoireId());

        if ("delete".equals(action.operation())) {
            current.setDeletedAt(OffsetDateTime.now());
            current.setServerUpdatedAt(OffsetDateTime.now());
            apprenantRepository.save(current);
            return new SyncResultDto(action.id(), "applied", null);
        }

        boolean conflict = isConflict(current.getServerUpdatedAt(), current.getClientUpdatedAt(),
                action.horodatageClient());

        if (conflict) {
            boolean clientWins = action.horodatageClient() != null &&
                    (current.getClientUpdatedAt() == null ||
                     action.horodatageClient().isAfter(current.getClientUpdatedAt()));

            Map<String, Object> versionServeur = toMap(current);
            Map<String, Object> versionClient = action.payload();

            if (clientWins) {
                mapApprenantPayload(current, action.payload());
                current.setServerUpdatedAt(OffsetDateTime.now());
                current.setClientUpdatedAt(action.horodatageClient());
                current.setSyncStatus("synced");
                apprenantRepository.save(current);
            }

            logConflict(action, versionServeur, versionClient, "last_write_wins");
            return new SyncResultDto(action.id(), "conflict", "Résolu par Last Write Wins");
        }

        mapApprenantPayload(current, action.payload());
        current.setServerUpdatedAt(OffsetDateTime.now());
        current.setClientUpdatedAt(action.horodatageClient());
        current.setSyncStatus("synced");
        apprenantRepository.save(current);
        auditService.log("sync_update", "apprenant", entiteId, Map.of("source", "sync"));
        return new SyncResultDto(action.id(), "applied", null);
    }

    private void mapApprenantPayload(ApprenantEntity e, Map<String, Object> payload) {
        if (payload.containsKey("territoireId")) e.setTerritoireId(toUUID(payload.get("territoireId")));
        if (payload.containsKey("nom")) e.setNom((String) payload.get("nom"));
        if (payload.containsKey("prenom")) e.setPrenom((String) payload.get("prenom"));
        if (payload.containsKey("sexe")) e.setSexe((String) payload.get("sexe"));
        if (payload.containsKey("telephone")) e.setTelephone((String) payload.get("telephone"));
        if (payload.containsKey("photoUrl")) e.setPhotoUrl((String) payload.get("photoUrl"));
    }

    private Map<String, Object> toMap(ApprenantEntity e) {
        return Map.of(
                "id", e.getId().toString(),
                "nom", e.getNom() != null ? e.getNom() : "",
                "prenom", e.getPrenom() != null ? e.getPrenom() : "",
                "serverUpdatedAt", e.getServerUpdatedAt() != null ? e.getServerUpdatedAt().toString() : "",
                "clientUpdatedAt", e.getClientUpdatedAt() != null ? e.getClientUpdatedAt().toString() : ""
        );
    }

    // ==================== ENCADREUR ====================

    private SyncResultDto syncEncadreur(SyncActionDto action) {
        UUID entiteId = action.entiteId();
        Optional<EncadreurEntity> existing = encadreurRepository.findById(entiteId);

        if (existing.isEmpty()) {
            UUID territoireId = toUUID(action.payload().get("territoireId"));
            if (territoireId != null) {
                territoireAccessService.requireAccess(territoireId);
            }
            EncadreurEntity e = new EncadreurEntity();
            e.setId(entiteId);
            mapEncadreurPayload(e, action.payload());
            e.setSyncStatus("synced");
            e.setServerUpdatedAt(OffsetDateTime.now());
            e.setClientUpdatedAt(action.horodatageClient());
            encadreurRepository.save(e);
            return new SyncResultDto(action.id(), "applied", null);
        }

        EncadreurEntity current = existing.get();
        territoireAccessService.requireAccess(current.getTerritoireId());

        if ("delete".equals(action.operation())) {
            current.setDeletedAt(OffsetDateTime.now());
            current.setServerUpdatedAt(OffsetDateTime.now());
            encadreurRepository.save(current);
            return new SyncResultDto(action.id(), "applied", null);
        }

        boolean conflict = isConflict(current.getServerUpdatedAt(), current.getClientUpdatedAt(),
                action.horodatageClient());

        if (conflict) {
            boolean clientWins = action.horodatageClient() != null &&
                    (current.getClientUpdatedAt() == null ||
                     action.horodatageClient().isAfter(current.getClientUpdatedAt()));
            Map<String, Object> versionServeur = Map.of(
                    "nom", current.getNom() != null ? current.getNom() : "",
                    "serverUpdatedAt", current.getServerUpdatedAt() != null ? current.getServerUpdatedAt().toString() : "");
            if (clientWins) {
                mapEncadreurPayload(current, action.payload());
                current.setServerUpdatedAt(OffsetDateTime.now());
                current.setClientUpdatedAt(action.horodatageClient());
                encadreurRepository.save(current);
            }
            logConflict(action, versionServeur, action.payload(), "last_write_wins");
            return new SyncResultDto(action.id(), "conflict", "Résolu par Last Write Wins");
        }

        mapEncadreurPayload(current, action.payload());
        current.setServerUpdatedAt(OffsetDateTime.now());
        current.setClientUpdatedAt(action.horodatageClient());
        encadreurRepository.save(current);
        return new SyncResultDto(action.id(), "applied", null);
    }

    private void mapEncadreurPayload(EncadreurEntity e, Map<String, Object> payload) {
        if (payload.containsKey("territoireId")) e.setTerritoireId(toUUID(payload.get("territoireId")));
        if (payload.containsKey("nom")) e.setNom((String) payload.get("nom"));
        if (payload.containsKey("prenom")) e.setPrenom((String) payload.get("prenom"));
        if (payload.containsKey("telephone")) e.setTelephone((String) payload.get("telephone"));
        if (payload.containsKey("specialite")) e.setSpecialite((String) payload.get("specialite"));
        if (payload.containsKey("disponibilite")) e.setDisponibilite((String) payload.get("disponibilite"));
    }

    // ==================== SESSION ====================

    private SyncResultDto syncSession(SyncActionDto action) {
        UUID entiteId = action.entiteId();
        Optional<SessionFormationEntity> existing = sessionRepository.findById(entiteId);

        if (existing.isEmpty()) {
            UUID territoireId = toUUID(action.payload().get("territoireId"));
            if (territoireId != null) {
                territoireAccessService.requireAccess(territoireId);
            }
            SessionFormationEntity e = new SessionFormationEntity();
            e.setId(entiteId);
            mapSessionPayload(e, action.payload());
            e.setSyncStatus("synced");
            e.setServerUpdatedAt(OffsetDateTime.now());
            e.setClientUpdatedAt(action.horodatageClient());
            sessionRepository.save(e);
            return new SyncResultDto(action.id(), "applied", null);
        }

        SessionFormationEntity current = existing.get();
        territoireAccessService.requireAccess(current.getTerritoireId());

        boolean conflict = isConflict(current.getServerUpdatedAt(), current.getClientUpdatedAt(),
                action.horodatageClient());

        if (conflict) {
            boolean clientWins = action.horodatageClient() != null &&
                    (current.getClientUpdatedAt() == null ||
                     action.horodatageClient().isAfter(current.getClientUpdatedAt()));
            Map<String, Object> versionServeur = Map.of(
                    "lieu", current.getLieu() != null ? current.getLieu() : "",
                    "serverUpdatedAt", current.getServerUpdatedAt() != null ? current.getServerUpdatedAt().toString() : "");
            if (clientWins) {
                mapSessionPayload(current, action.payload());
                current.setServerUpdatedAt(OffsetDateTime.now());
                current.setClientUpdatedAt(action.horodatageClient());
                sessionRepository.save(current);
            }
            logConflict(action, versionServeur, action.payload(), "last_write_wins");
            return new SyncResultDto(action.id(), "conflict", "Résolu par Last Write Wins");
        }

        mapSessionPayload(current, action.payload());
        current.setServerUpdatedAt(OffsetDateTime.now());
        current.setClientUpdatedAt(action.horodatageClient());
        sessionRepository.save(current);
        return new SyncResultDto(action.id(), "applied", null);
    }

    private void mapSessionPayload(SessionFormationEntity e, Map<String, Object> payload) {
        if (payload.containsKey("territoireId")) e.setTerritoireId(toUUID(payload.get("territoireId")));
        if (payload.containsKey("encadreurId")) e.setEncadreurId(toUUID(payload.get("encadreurId")));
        if (payload.containsKey("lieu")) e.setLieu((String) payload.get("lieu"));
        if (payload.containsKey("programme")) e.setProgramme((String) payload.get("programme"));
        if (payload.containsKey("statut")) e.setStatut((String) payload.get("statut"));
    }

    // ==================== PRESENCE ====================

    private SyncResultDto syncPresence(SyncActionDto action, UUID utilisateurId) {
        UUID entiteId = action.entiteId();
        Optional<PresenceEntity> existing = presenceRepository.findById(entiteId);

        if (existing.isEmpty()) {
            PresenceEntity e = new PresenceEntity();
            e.setId(entiteId);
            mapPresencePayload(e, action.payload());
            e.setSaisieParId(utilisateurId);
            e.setSyncStatus("synced");
            e.setServerUpdatedAt(OffsetDateTime.now());
            e.setClientUpdatedAt(action.horodatageClient());
            presenceRepository.save(e);
            return new SyncResultDto(action.id(), "applied", null);
        }

        PresenceEntity current = existing.get();
        UUID sessionId = current.getSessionId();
        if (sessionId != null) {
            sessionRepository.findById(sessionId).ifPresent(s ->
                    territoireAccessService.requireAccess(s.getTerritoireId()));
        }
        boolean conflict = isConflict(current.getServerUpdatedAt(), current.getClientUpdatedAt(),
                action.horodatageClient());

        if (conflict) {
            boolean clientWins = action.horodatageClient() != null &&
                    (current.getClientUpdatedAt() == null ||
                     action.horodatageClient().isAfter(current.getClientUpdatedAt()));
            Map<String, Object> versionServeur = Map.of(
                    "statut", current.getStatut() != null ? current.getStatut() : "",
                    "serverUpdatedAt", current.getServerUpdatedAt() != null ? current.getServerUpdatedAt().toString() : "");
            if (clientWins) {
                mapPresencePayload(current, action.payload());
                current.setServerUpdatedAt(OffsetDateTime.now());
                current.setClientUpdatedAt(action.horodatageClient());
                presenceRepository.save(current);
            }
            logConflict(action, versionServeur, action.payload(), "last_write_wins");
            return new SyncResultDto(action.id(), "conflict", "Résolu par Last Write Wins");
        }

        mapPresencePayload(current, action.payload());
        current.setServerUpdatedAt(OffsetDateTime.now());
        current.setClientUpdatedAt(action.horodatageClient());
        presenceRepository.save(current);
        return new SyncResultDto(action.id(), "applied", null);
    }

    private void mapPresencePayload(PresenceEntity e, Map<String, Object> payload) {
        if (payload.containsKey("sessionId")) e.setSessionId(toUUID(payload.get("sessionId")));
        if (payload.containsKey("apprenantId")) e.setApprenantId(toUUID(payload.get("apprenantId")));
        if (payload.containsKey("statut")) e.setStatut((String) payload.get("statut"));
        if (payload.containsKey("saisieParId")) e.setSaisieParId(toUUID(payload.get("saisieParId")));
        if (payload.containsKey("date") && payload.get("date") != null) {
            e.setDate(java.time.LocalDate.parse(payload.get("date").toString()));
        }
    }

    // ==================== RESULTAT EXAMEN ====================

    private SyncResultDto syncResultat(SyncActionDto action) {
        UUID entiteId = action.entiteId();
        Optional<ResultatExamenEntity> existing = resultatRepository.findById(entiteId);

        if (existing.isEmpty()) {
            ResultatExamenEntity e = new ResultatExamenEntity();
            e.setId(entiteId);
            mapResultatPayload(e, action.payload());
            e.setSyncStatus("synced");
            e.setServerUpdatedAt(OffsetDateTime.now());
            e.setClientUpdatedAt(action.horodatageClient());
            resultatRepository.save(e);
            return new SyncResultDto(action.id(), "applied", null);
        }

        ResultatExamenEntity current = existing.get();
        UUID sessionId = current.getSessionId();
        if (sessionId != null) {
            sessionRepository.findById(sessionId).ifPresent(s ->
                    territoireAccessService.requireAccess(s.getTerritoireId()));
        }
        boolean conflict = isConflict(current.getServerUpdatedAt(), current.getClientUpdatedAt(),
                action.horodatageClient());

        if (conflict) {
            boolean clientWins = action.horodatageClient() != null &&
                    (current.getClientUpdatedAt() == null ||
                     action.horodatageClient().isAfter(current.getClientUpdatedAt()));
            Map<String, Object> versionServeur = Map.of(
                    "note", current.getNote() != null ? current.getNote().toString() : "",
                    "serverUpdatedAt", current.getServerUpdatedAt() != null ? current.getServerUpdatedAt().toString() : "");
            if (clientWins) {
                mapResultatPayload(current, action.payload());
                current.setServerUpdatedAt(OffsetDateTime.now());
                current.setClientUpdatedAt(action.horodatageClient());
                resultatRepository.save(current);
            }
            logConflict(action, versionServeur, action.payload(), "last_write_wins");
            return new SyncResultDto(action.id(), "conflict", "Résolu par Last Write Wins");
        }

        mapResultatPayload(current, action.payload());
        current.setServerUpdatedAt(OffsetDateTime.now());
        current.setClientUpdatedAt(action.horodatageClient());
        resultatRepository.save(current);
        return new SyncResultDto(action.id(), "applied", null);
    }

    private void mapResultatPayload(ResultatExamenEntity e, Map<String, Object> payload) {
        if (payload.containsKey("sessionId")) e.setSessionId(toUUID(payload.get("sessionId")));
        if (payload.containsKey("apprenantId")) e.setApprenantId(toUUID(payload.get("apprenantId")));
        if (payload.containsKey("note")) {
            Object note = payload.get("note");
            if (note instanceof Number n) {
                e.setNote(java.math.BigDecimal.valueOf(n.doubleValue()));
            }
        }
    }

    // ==================== UTILITAIRES ====================

    /**
     * Détecte un conflit : il y a conflit si le serveur a été mis à jour
     * après le client_updated_at actuellement stocké (modification concurrente
     * par un autre encadreur entre-temps).
     */
    private boolean isConflict(OffsetDateTime serverUpdatedAt, OffsetDateTime currentClientUpdatedAt,
                               OffsetDateTime newClientUpdatedAt) {
        if (serverUpdatedAt == null || currentClientUpdatedAt == null) {
            return false;
        }
        return serverUpdatedAt.isAfter(currentClientUpdatedAt);
    }

    /**
     * Journalise un conflit dans sync_conflict_log.
     */
    private void logConflict(SyncActionDto action, Map<String, Object> versionServeur,
                             Map<String, Object> versionClient, String resolution) {
        SyncConflictLogEntity log = new SyncConflictLogEntity();
        log.setId(UUID.randomUUID());
        log.setSyncQueueId(action.id());
        log.setResolution(resolution);
        log.setDateResolution(OffsetDateTime.now());
        log.setVersionServeur(versionServeur);
        log.setVersionClient(versionClient);
        conflictLogRepository.save(log);
    }

    private UUID toUUID(Object value) {
        if (value == null) return null;
        if (value instanceof UUID u) return u;
        return UUID.fromString(value.toString());
    }
}
