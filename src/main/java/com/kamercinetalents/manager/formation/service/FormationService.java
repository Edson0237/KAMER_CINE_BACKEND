package com.kamercinetalents.manager.formation.service;

import com.kamercinetalents.manager.admin.repository.ParametreSystemeRepository;
import com.kamercinetalents.manager.common.dto.PageResponseDto;
import com.kamercinetalents.manager.common.service.AuditService;
import com.kamercinetalents.manager.common.service.TerritoireAccessService;
import com.kamercinetalents.manager.formation.domain.*;
import com.kamercinetalents.manager.formation.dto.*;
import com.kamercinetalents.manager.formation.repository.*;
import com.kamercinetalents.manager.common.security.SecurityUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service métier pour le module M3 Formation.
 *
 * <p>Gère les apprenants, encadreurs, sessions, inscriptions, présences,
 * résultats d'examens et attestations. Toutes les opérations CUD sont
 * journalisées dans audit_log. Le contrôle du périmètre territorial est
 * appliqué à chaque méthode via {@link TerritoireAccessService}.</p>
 */
@Service
@Transactional
public class FormationService {

    private final ApprenantRepository apprenantRepository;
    private final EncadreurRepository encadreurRepository;
    private final SessionFormationRepository sessionRepository;
    private final InscriptionSessionRepository inscriptionRepository;
    private final PresenceRepository presenceRepository;
    private final ResultatExamenRepository resultatRepository;
    private final AttestationRepository attestationRepository;
    private final TerritoireAccessService territoireAccessService;
    private final AuditService auditService;
    private final ParametreSystemeRepository parametreSystemeRepository;

    public FormationService(
            ApprenantRepository apprenantRepository,
            EncadreurRepository encadreurRepository,
            SessionFormationRepository sessionRepository,
            InscriptionSessionRepository inscriptionRepository,
            PresenceRepository presenceRepository,
            ResultatExamenRepository resultatRepository,
            AttestationRepository attestationRepository,
            TerritoireAccessService territoireAccessService,
            AuditService auditService,
            ParametreSystemeRepository parametreSystemeRepository) {
        this.apprenantRepository = apprenantRepository;
        this.encadreurRepository = encadreurRepository;
        this.sessionRepository = sessionRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.presenceRepository = presenceRepository;
        this.resultatRepository = resultatRepository;
        this.attestationRepository = attestationRepository;
        this.territoireAccessService = territoireAccessService;
        this.auditService = auditService;
        this.parametreSystemeRepository = parametreSystemeRepository;
    }

    // ==================== APPRENANT ====================

    @Transactional(readOnly = true)
    public ApprenantDto getApprenant(UUID id) {
        ApprenantEntity e = apprenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Apprenant introuvable: " + id));
        territoireAccessService.requireAccess(e.getTerritoireId());
        return toDto(e);
    }

    @Transactional(readOnly = true)
    public List<ApprenantDto> listApprenantsByTerritoire(UUID territoireId) {
        territoireAccessService.requireAccess(territoireId);
        return apprenantRepository.findByTerritoireId(territoireId).stream()
                .filter(a -> a.getDeletedAt() == null)
                .map(this::toDto)
                .toList();
    }

    public ApprenantDto createApprenant(CreateApprenantRequest req) {
        territoireAccessService.requireAccess(req.territoireId());
        ApprenantEntity e = new ApprenantEntity();
        e.setId(req.id() != null ? req.id() : UUID.randomUUID());
        e.setTerritoireId(req.territoireId());
        e.setNom(req.nom());
        e.setPrenom(req.prenom());
        e.setDateNaissance(req.dateNaissance());
        e.setSexe(req.sexe());
        e.setTelephone(req.telephone());
        e.setPhotoUrl(req.photoUrl());
        e.setCompetences(req.competences());
        e.setPortfolio(req.portfolio());
        e.setMetadata(req.metadata());
        e.setSyncStatus("synced");
        ApprenantEntity saved = apprenantRepository.save(e);
        auditService.log("create", "apprenant", saved.getId(),
                Map.of("nom", saved.getNom(), "prenom", saved.getPrenom()));
        return toDto(saved);
    }

    public void softDeleteApprenant(UUID id) {
        ApprenantEntity e = apprenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Apprenant introuvable: " + id));
        territoireAccessService.requireAccess(e.getTerritoireId());
        e.setDeletedAt(java.time.OffsetDateTime.now());
        apprenantRepository.save(e);
        auditService.log("delete", "apprenant", id, Map.of());
    }

    public ApprenantDto updateApprenant(UUID id, CreateApprenantRequest req) {
        ApprenantEntity e = apprenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Apprenant introuvable: " + id));
        territoireAccessService.requireAccess(e.getTerritoireId());
        e.setNom(req.nom());
        e.setPrenom(req.prenom());
        e.setDateNaissance(req.dateNaissance());
        e.setSexe(req.sexe());
        e.setTelephone(req.telephone());
        e.setPhotoUrl(req.photoUrl());
        if (req.competences() != null) e.setCompetences(req.competences());
        if (req.portfolio() != null) e.setPortfolio(req.portfolio());
        if (req.metadata() != null) e.setMetadata(req.metadata());
        e.setServerUpdatedAt(java.time.OffsetDateTime.now());
        ApprenantEntity saved = apprenantRepository.save(e);
        auditService.log("update", "apprenant", saved.getId(), Map.of());
        return toDto(saved);
    }

    /**
     * Recherche paginée des apprenants d'un territoire, avec filtre optionnel
     * par nom/prénom. Nécessaire pour les listes principales — un territoire
     * pouvant compter plusieurs centaines d'apprenants à terme.
     */
    @Transactional(readOnly = true)
    public PageResponseDto<ApprenantDto> searchApprenants(UUID territoireId, String nom, int page, int size) {
        territoireAccessService.requireAccess(territoireId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("nom").ascending());
        String needle = (nom == null || nom.isBlank()) ? null : nom.trim();
        return PageResponseDto.from(apprenantRepository.search(territoireId, needle, pageable), this::toDto);
    }

    private ApprenantDto toDto(ApprenantEntity e) {
        return new ApprenantDto(e.getId(), e.getTerritoireId(), e.getNom(), e.getPrenom(),
                e.getDateNaissance(), e.getSexe(), e.getTelephone(), e.getPhotoUrl(),
                e.getCompetences(), e.getPortfolio(), e.getMetadata());
    }

    // ==================== ENCADREUR ====================

    @Transactional(readOnly = true)
    public EncadreurDto getEncadreur(UUID id) {
        EncadreurEntity e = encadreurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Encadreur introuvable: " + id));
        territoireAccessService.requireAccess(e.getTerritoireId());
        return toDto(e);
    }

    @Transactional(readOnly = true)
    public List<EncadreurDto> listEncadreursByTerritoire(UUID territoireId) {
        territoireAccessService.requireAccess(territoireId);
        return encadreurRepository.findByTerritoireId(territoireId).stream()
                .filter(e -> e.getDeletedAt() == null)
                .map(this::toDto)
                .toList();
    }

    public EncadreurDto createEncadreur(CreateEncadreurRequest req) {
        territoireAccessService.requireAccess(req.territoireId());
        EncadreurEntity e = new EncadreurEntity();
        e.setId(req.id() != null ? req.id() : UUID.randomUUID());
        e.setTerritoireId(req.territoireId());
        e.setNom(req.nom());
        e.setPrenom(req.prenom());
        e.setTelephone(req.telephone());
        e.setSpecialite(req.specialite());
        e.setDisponibilite(req.disponibilite());
        e.setPhotoUrl(req.photoUrl());
        e.setMetadata(req.metadata());
        e.setSyncStatus("synced");
        EncadreurEntity saved = encadreurRepository.save(e);
        auditService.log("create", "encadreur", saved.getId(),
                Map.of("nom", saved.getNom(), "prenom", saved.getPrenom()));
        return toDto(saved);
    }

    public EncadreurDto updateEncadreur(UUID id, CreateEncadreurRequest req) {
        EncadreurEntity e = encadreurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Encadreur introuvable: " + id));
        territoireAccessService.requireAccess(e.getTerritoireId());
        e.setNom(req.nom());
        e.setPrenom(req.prenom());
        e.setTelephone(req.telephone());
        e.setSpecialite(req.specialite());
        e.setDisponibilite(req.disponibilite());
        e.setPhotoUrl(req.photoUrl());
        if (req.metadata() != null) e.setMetadata(req.metadata());
        e.setServerUpdatedAt(java.time.OffsetDateTime.now());
        EncadreurEntity saved = encadreurRepository.save(e);
        auditService.log("update", "encadreur", saved.getId(), Map.of());
        return toDto(saved);
    }

    public void softDeleteEncadreur(UUID id) {
        EncadreurEntity e = encadreurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Encadreur introuvable: " + id));
        territoireAccessService.requireAccess(e.getTerritoireId());
        e.setDeletedAt(java.time.OffsetDateTime.now());
        encadreurRepository.save(e);
        auditService.log("delete", "encadreur", id, Map.of());
    }

    /**
     * Recherche paginée des encadreurs d'un territoire, avec filtre optionnel
     * par nom/prénom.
     */
    @Transactional(readOnly = true)
    public PageResponseDto<EncadreurDto> searchEncadreurs(UUID territoireId, String nom, int page, int size) {
        territoireAccessService.requireAccess(territoireId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("nom").ascending());
        String needle = (nom == null || nom.isBlank()) ? null : nom.trim();
        return PageResponseDto.from(encadreurRepository.search(territoireId, needle, pageable), this::toDto);
    }

    private EncadreurDto toDto(EncadreurEntity e) {
        return new EncadreurDto(e.getId(), e.getTerritoireId(), e.getNom(), e.getPrenom(),
                e.getTelephone(), e.getSpecialite(), e.getDisponibilite(),
                e.getEvaluationMoyenne(), e.getPhotoUrl(), e.getMetadata());
    }

    // ==================== SESSION ====================

    @Transactional(readOnly = true)
    public SessionFormationDto getSession(UUID id) {
        SessionFormationEntity e = sessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Session introuvable: " + id));
        territoireAccessService.requireAccess(e.getTerritoireId());
        return toDto(e);
    }

    @Transactional(readOnly = true)
    public List<SessionFormationDto> listSessionsByTerritoire(UUID territoireId) {
        territoireAccessService.requireAccess(territoireId);
        return sessionRepository.findByTerritoireId(territoireId).stream()
                .map(this::toDto)
                .toList();
    }

    public SessionFormationDto createSession(CreateSessionRequest req) {
        territoireAccessService.requireAccess(req.territoireId());
        SessionFormationEntity e = new SessionFormationEntity();
        e.setId(req.id() != null ? req.id() : UUID.randomUUID());
        e.setTerritoireId(req.territoireId());
        e.setEncadreurId(req.encadreurId());
        e.setDateDebut(req.dateDebut());
        e.setDateFin(req.dateFin());
        e.setLieu(req.lieu());
        e.setProgramme(req.programme());
        e.setStatut(req.statut() != null ? req.statut() : "planifiee");
        e.setSyncStatus("synced");
        SessionFormationEntity saved = sessionRepository.save(e);
        auditService.log("create", "session_formation", saved.getId(),
                Map.of("territoire", saved.getTerritoireId().toString()));
        return toDto(saved);
    }

    public SessionFormationDto updateSession(UUID id, CreateSessionRequest req) {
        SessionFormationEntity e = sessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Session introuvable: " + id));
        territoireAccessService.requireAccess(e.getTerritoireId());
        if (req.encadreurId() != null) e.setEncadreurId(req.encadreurId());
        if (req.dateDebut() != null) e.setDateDebut(req.dateDebut());
        e.setDateFin(req.dateFin());
        e.setLieu(req.lieu());
        e.setProgramme(req.programme());
        if (req.statut() != null) e.setStatut(req.statut());
        e.setServerUpdatedAt(java.time.OffsetDateTime.now());
        SessionFormationEntity saved = sessionRepository.save(e);
        auditService.log("update", "session_formation", saved.getId(), Map.of());
        return toDto(saved);
    }

    /**
     * Recherche paginée des sessions d'un territoire, avec filtre optionnel
     * par lieu/programme.
     */
    @Transactional(readOnly = true)
    public PageResponseDto<SessionFormationDto> searchSessions(UUID territoireId, String recherche, int page, int size) {
        territoireAccessService.requireAccess(territoireId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateDebut").descending());
        String needle = (recherche == null || recherche.isBlank()) ? null : recherche.trim();
        return PageResponseDto.from(sessionRepository.search(territoireId, needle, pageable), this::toDto);
    }

    public SessionFormationDto cloturerSession(UUID sessionId) {
        SessionFormationEntity e = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session introuvable: " + sessionId));
        territoireAccessService.requireAccess(e.getTerritoireId());
        e.setStatut("cloturee");
        SessionFormationEntity saved = sessionRepository.save(e);
        auditService.log("update", "session_formation", saved.getId(),
                Map.of("action", "cloture", "statut", "cloturee"));
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public TauxReussiteDto calculerTauxReussite(UUID sessionId) {
        SessionFormationEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session introuvable: " + sessionId));
        territoireAccessService.requireAccess(session.getTerritoireId());

        List<InscriptionSessionEntity> inscriptions = inscriptionRepository.findBySessionId(sessionId);
        int totalApprenants = inscriptions.size();

        BigDecimal seuilReussite = parametreSystemeRepository.findByCle("formation.seuil_reussite")
                .map(p -> new BigDecimal(p.getValeur()))
                .orElse(new BigDecimal("10"));

        List<ResultatExamenEntity> resultats = resultatRepository.findBySessionId(sessionId);
        int totalReussis = (int) resultats.stream()
                .filter(r -> r.getNote() != null && r.getNote().compareTo(seuilReussite) >= 0)
                .count();

        double taux = totalApprenants > 0 ? (totalReussis * 100.0) / totalApprenants : 0.0;
        boolean cloturee = "cloturee".equals(session.getStatut());

        return new TauxReussiteDto(sessionId, totalApprenants, totalReussis, taux, cloturee);
    }

    private SessionFormationDto toDto(SessionFormationEntity e) {
        return new SessionFormationDto(e.getId(), e.getTerritoireId(), e.getEncadreurId(),
                e.getDateDebut(), e.getDateFin(), e.getLieu(), e.getProgramme(), e.getStatut());
    }

    // ==================== INSCRIPTION ====================

    public void inscrireApprenant(UUID sessionId, UUID apprenantId) {
        SessionFormationEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session introuvable: " + sessionId));
        territoireAccessService.requireAccess(session.getTerritoireId());
        ApprenantEntity apprenant = apprenantRepository.findById(apprenantId)
                .orElseThrow(() -> new IllegalArgumentException("Apprenant introuvable: " + apprenantId));
        territoireAccessService.requireAccess(apprenant.getTerritoireId());
        InscriptionSessionEntity e = new InscriptionSessionEntity();
        e.setId(UUID.randomUUID());
        e.setSessionId(sessionId);
        e.setApprenantId(apprenantId);
        inscriptionRepository.save(e);
        auditService.log("create", "inscription_session", e.getId(),
                Map.of("session", sessionId.toString(), "apprenant", apprenantId.toString()));
    }

    @Transactional(readOnly = true)
    public List<InscriptionSessionEntity> listInscriptionsBySession(UUID sessionId) {
        return inscriptionRepository.findBySessionId(sessionId);
    }

    // ==================== PRESENCE ====================

    public PresenceDto saisirPresence(CreatePresenceRequest req) {
        SessionFormationEntity session = sessionRepository.findById(req.sessionId())
                .orElseThrow(() -> new IllegalArgumentException("Session introuvable: " + req.sessionId()));
        territoireAccessService.requireAccess(session.getTerritoireId());
        PresenceEntity e = new PresenceEntity();
        e.setId(req.id() != null ? req.id() : UUID.randomUUID());
        e.setSessionId(req.sessionId());
        e.setApprenantId(req.apprenantId());
        e.setDate(req.date());
        e.setStatut(req.statut());
        e.setSaisieParId(SecurityUtils.getCurrentUserId());
        e.setSyncStatus("synced");
        PresenceEntity saved = presenceRepository.save(e);
        auditService.log("create", "presence", saved.getId(),
                Map.of("statut", saved.getStatut()));
        return new PresenceDto(saved.getId(), saved.getSessionId(), saved.getApprenantId(),
                saved.getDate(), saved.getStatut(), saved.getSaisieParId());
    }

    @Transactional(readOnly = true)
    public List<PresenceDto> listPresencesBySession(UUID sessionId) {
        SessionFormationEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session introuvable: " + sessionId));
        territoireAccessService.requireAccess(session.getTerritoireId());
        return presenceRepository.findBySessionId(sessionId).stream()
                .map(e -> new PresenceDto(e.getId(), e.getSessionId(), e.getApprenantId(),
                        e.getDate(), e.getStatut(), e.getSaisieParId()))
                .toList();
    }

    // ==================== RÉSULTAT EXAMEN ====================

    public ResultatExamenDto saisirResultat(CreateResultatExamenRequest req) {
        SessionFormationEntity session = sessionRepository.findById(req.sessionId())
                .orElseThrow(() -> new IllegalArgumentException("Session introuvable: " + req.sessionId()));
        territoireAccessService.requireAccess(session.getTerritoireId());
        ResultatExamenEntity e = new ResultatExamenEntity();
        e.setId(req.id() != null ? req.id() : UUID.randomUUID());
        e.setSessionId(req.sessionId());
        e.setApprenantId(req.apprenantId());
        e.setNote(req.note());
        e.setDateExamen(req.dateExamen());
        e.setSyncStatus("synced");
        ResultatExamenEntity saved = resultatRepository.save(e);
        auditService.log("create", "resultat_examen", saved.getId(),
                Map.of("note", saved.getNote() != null ? saved.getNote().toString() : "null"));
        return new ResultatExamenDto(saved.getId(), saved.getSessionId(), saved.getApprenantId(),
                saved.getNote(), saved.getDateExamen());
    }

    @Transactional(readOnly = true)
    public List<ResultatExamenDto> listResultatsBySession(UUID sessionId) {
        SessionFormationEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session introuvable: " + sessionId));
        territoireAccessService.requireAccess(session.getTerritoireId());
        return resultatRepository.findBySessionId(sessionId).stream()
                .map(e -> new ResultatExamenDto(e.getId(), e.getSessionId(), e.getApprenantId(),
                        e.getNote(), e.getDateExamen()))
                .toList();
    }

    // ==================== ATTESTATION ====================

    public AttestationDto genererAttestation(UUID apprenantId, UUID sessionId) {
        int niveau = SecurityUtils.get().niveauHierarchique();
        if (niveau > 5) {
            throw new com.kamercinetalents.manager.common.exception.PerimeterAccessException(
                    "Génération d'attestation réservée aux niveaux 1 à 5 (Comité Central à Commune). Niveau actuel: N" + niveau);
        }
        SessionFormationEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session introuvable: " + sessionId));
        territoireAccessService.requireAccess(session.getTerritoireId());
        ApprenantEntity apprenant = apprenantRepository.findById(apprenantId)
                .orElseThrow(() -> new IllegalArgumentException("Apprenant introuvable: " + apprenantId));
        territoireAccessService.requireAccess(apprenant.getTerritoireId());
        AttestationEntity e = new AttestationEntity();
        e.setId(UUID.randomUUID());
        e.setApprenantId(apprenantId);
        e.setSessionId(sessionId);
        e.setNumero("ATT-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        e.setDateDelivrance(LocalDate.now());
        AttestationEntity saved = attestationRepository.save(e);
        auditService.log("create", "attestation", saved.getId(),
                Map.of("numero", saved.getNumero()));
        return new AttestationDto(saved.getId(), saved.getApprenantId(), saved.getSessionId(),
                saved.getNumero(), saved.getDateDelivrance(), saved.getFichierUrl());
    }

    @Transactional(readOnly = true)
    public AttestationDto getAttestation(UUID id) {
        AttestationEntity e = attestationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attestation introuvable: " + id));
        SessionFormationEntity session = sessionRepository.findById(e.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Session introuvable: " + e.getSessionId()));
        territoireAccessService.requireAccess(session.getTerritoireId());
        return new AttestationDto(e.getId(), e.getApprenantId(), e.getSessionId(),
                e.getNumero(), e.getDateDelivrance(), e.getFichierUrl());
    }
}
