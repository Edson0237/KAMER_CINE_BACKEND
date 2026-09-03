package com.kamercinetalents.manager.ecosysteme.service;

import com.kamercinetalents.manager.common.exception.PerimeterAccessException;
import com.kamercinetalents.manager.common.security.SecurityContext;
import com.kamercinetalents.manager.common.security.SecurityUtils;
import com.kamercinetalents.manager.common.service.AuditService;
import com.kamercinetalents.manager.ecosysteme.domain.*;
import com.kamercinetalents.manager.ecosysteme.dto.*;
import com.kamercinetalents.manager.ecosysteme.repository.*;
import com.kamercinetalents.manager.formation.domain.ApprenantEntity;
import com.kamercinetalents.manager.iam.domain.RoleEntity;
import com.kamercinetalents.manager.iam.domain.UtilisateurEntity;
import com.kamercinetalents.manager.iam.repository.RoleRepository;
import com.kamercinetalents.manager.iam.repository.UtilisateurRepository;
import com.kamercinetalents.manager.notification.service.NotificationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service du module Écosystème — gestion du contenu du site public.
 *
 * <p>Deux types d'accès :</p>
 * <ul>
 *   <li><strong>Public</strong> : lecture des actualités publiées, FAQ active,
 *       équipe, partenaires. Écriture des candidatures et messages de contact.</li>
 *   <li><strong>Admin</strong> (niveau 1 uniquement) : CRUD sur tout le contenu,
 *       traitement des candidatures avec création de compte apprenant.</li>
 * </ul>
 */
@Service
@Transactional
public class EcosystemeService {

    private final ActualitePubliqueRepository actualiteRepo;
    private final FaqItemRepository faqRepo;
    private final MembreEquipeRepository membreRepo;
    private final PartenaireRepository partenaireRepo;
    private final CandidaturePubliqueRepository candidatureRepo;
    private final ContactMessageRepository contactRepo;
    private final RoleRepository roleRepo;
    private final UtilisateurRepository utilisateurRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final JdbcTemplate jdbcTemplate;
    private final NotificationService notificationService;

    public EcosystemeService(
            ActualitePubliqueRepository actualiteRepo,
            FaqItemRepository faqRepo,
            MembreEquipeRepository membreRepo,
            PartenaireRepository partenaireRepo,
            CandidaturePubliqueRepository candidatureRepo,
            ContactMessageRepository contactRepo,
            RoleRepository roleRepo,
            UtilisateurRepository utilisateurRepo,
            PasswordEncoder passwordEncoder,
            AuditService auditService,
            JdbcTemplate jdbcTemplate,
            NotificationService notificationService) {
        this.actualiteRepo = actualiteRepo;
        this.faqRepo = faqRepo;
        this.membreRepo = membreRepo;
        this.partenaireRepo = partenaireRepo;
        this.candidatureRepo = candidatureRepo;
        this.contactRepo = contactRepo;
        this.roleRepo = roleRepo;
        this.utilisateurRepo = utilisateurRepo;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.jdbcTemplate = jdbcTemplate;
        this.notificationService = notificationService;
    }

    // ==================== PUBLIC — LECTURE ====================

    @Transactional(readOnly = true)
    public List<ActualitePubliqueDto> getPublishedActualites() {
        return actualiteRepo.findByStatutAndDeletedAtIsNullOrderByDatePublicationDesc("publiee")
                .stream().map(this::toActualiteDto).toList();
    }

    @Transactional(readOnly = true)
    public ActualitePubliqueDto getActualiteById(UUID id) {
        ActualitePubliqueEntity e = actualiteRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Actualité introuvable: " + id));
        if (!"publiee".equals(e.getStatut()) || e.getDeletedAt() != null) {
            throw new IllegalArgumentException("Actualité introuvable: " + id);
        }
        return toActualiteDto(e);
    }

    @Transactional(readOnly = true)
    public List<FaqItemDto> getActiveFaq() {
        return faqRepo.findByActifTrueOrderByCategorieAscOrdreAsc()
                .stream().map(this::toFaqDto).toList();
    }

    @Transactional(readOnly = true)
    public List<MembreEquipeDto> getEquipe() {
        return membreRepo.findAllByOrderByOrdreAsc()
                .stream().map(this::toMembreDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PartenaireDto> getPartenaires() {
        return partenaireRepo.findAllByOrderByOrdreAsc()
                .stream().map(this::toPartenaireDto).toList();
    }

    // ==================== PUBLIC — ÉCRITURE ====================

    public CandidaturePubliqueDto soumettreCandidature(CreateCandidatureRequest req) {
        CandidaturePubliqueEntity e = new CandidaturePubliqueEntity();
        e.setId(UUID.randomUUID());
        e.setNom(req.nom());
        e.setPrenom(req.prenom());
        e.setEmail(req.email());
        e.setTelephone(req.telephone());
        e.setMotivation(req.motivation());
        e.setStatut("en_attente");
        e.setDateSoumission(OffsetDateTime.now());
        CandidaturePubliqueEntity saved = candidatureRepo.save(e);
        return toCandidatureDto(saved);
    }

    public ContactMessageDto soumettreMessage(CreateContactMessageRequest req) {
        ContactMessageEntity e = new ContactMessageEntity();
        e.setId(UUID.randomUUID());
        e.setNom(req.nom());
        e.setEmail(req.email());
        e.setSujet(req.sujet());
        e.setMessage(req.message());
        e.setStatut("non_traite");
        e.setDateReception(OffsetDateTime.now());
        ContactMessageEntity saved = contactRepo.save(e);
        return toContactDto(saved);
    }

    // ==================== ADMIN — ACTUALITÉS ====================

    @Transactional(readOnly = true)
    public List<ActualitePubliqueDto> getAllActualites() {
        requireN1();
        return actualiteRepo.findByDeletedAtIsNullOrderByDatePublicationDesc()
                .stream().map(this::toActualiteDto).toList();
    }

    public ActualitePubliqueDto createActualite(CreateActualiteRequest req) {
        requireN1();
        ActualitePubliqueEntity e = new ActualitePubliqueEntity();
        e.setId(UUID.randomUUID());
        e.setTitre(req.titre());
        e.setContenu(req.contenu());
        e.setImageUrl(req.imageUrl());
        e.setDatePublication(OffsetDateTime.now());
        e.setStatut("brouillon");
        ActualitePubliqueEntity saved = actualiteRepo.save(e);
        auditService.log("create", "actualite_publique", saved.getId(),
                Map.of("titre", saved.getTitre()));
        return toActualiteDto(saved);
    }

    public ActualitePubliqueDto updateActualite(UUID id, CreateActualiteRequest req) {
        requireN1();
        ActualitePubliqueEntity e = actualiteRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Actualité introuvable: " + id));
        e.setTitre(req.titre());
        e.setContenu(req.contenu());
        e.setImageUrl(req.imageUrl());
        ActualitePubliqueEntity saved = actualiteRepo.save(e);
        auditService.log("update", "actualite_publique", saved.getId(),
                Map.of("titre", saved.getTitre()));
        return toActualiteDto(saved);
    }

    public ActualitePubliqueDto togglePublishActualite(UUID id) {
        requireN1();
        ActualitePubliqueEntity e = actualiteRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Actualité introuvable: " + id));
        e.setStatut("publiee".equals(e.getStatut()) ? "brouillon" : "publiee");
        ActualitePubliqueEntity saved = actualiteRepo.save(e);
        auditService.log("toggle_publish", "actualite_publique", saved.getId(),
                Map.of("statut", saved.getStatut()));
        return toActualiteDto(saved);
    }

    public void deleteActualite(UUID id) {
        requireN1();
        ActualitePubliqueEntity e = actualiteRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Actualité introuvable: " + id));
        e.setDeletedAt(OffsetDateTime.now());
        actualiteRepo.save(e);
        auditService.log("delete", "actualite_publique", id, Map.of());
    }

    // ==================== ADMIN — FAQ ====================

    @Transactional(readOnly = true)
    public List<FaqItemDto> getAllFaq() {
        requireN1();
        return faqRepo.findAllByOrderByCategorieAscOrdreAsc()
                .stream().map(this::toFaqDto).toList();
    }

    public FaqItemDto createFaqItem(CreateFaqItemRequest req) {
        requireN1();
        FaqItemEntity e = new FaqItemEntity();
        e.setId(UUID.randomUUID());
        e.setQuestion(req.question());
        e.setReponse(req.reponse());
        e.setCategorie(req.categorie());
        e.setOrdre(req.ordre());
        e.setActif(true);
        FaqItemEntity saved = faqRepo.save(e);
        auditService.log("create", "faq_item", saved.getId(), Map.of("question", saved.getQuestion()));
        return toFaqDto(saved);
    }

    public FaqItemDto updateFaqItem(UUID id, CreateFaqItemRequest req) {
        requireN1();
        FaqItemEntity e = faqRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FAQ item introuvable: " + id));
        e.setQuestion(req.question());
        e.setReponse(req.reponse());
        e.setCategorie(req.categorie());
        e.setOrdre(req.ordre());
        FaqItemEntity saved = faqRepo.save(e);
        auditService.log("update", "faq_item", saved.getId(), Map.of());
        return toFaqDto(saved);
    }

    public void deleteFaqItem(UUID id) {
        requireN1();
        faqRepo.deleteById(id);
        auditService.log("delete", "faq_item", id, Map.of());
    }

    // ==================== ADMIN — MEMBRES ÉQUIPE ====================

    @Transactional(readOnly = true)
    public List<MembreEquipeDto> getAllMembres() {
        requireN1();
        return membreRepo.findAllByOrderByOrdreAsc().stream().map(this::toMembreDto).toList();
    }

    public MembreEquipeDto createMembre(CreateMembreEquipeRequest req) {
        requireN1();
        MembreEquipeEntity e = new MembreEquipeEntity();
        e.setId(UUID.randomUUID());
        e.setNom(req.nom());
        e.setPoste(req.poste());
        e.setPhotoUrl(req.photoUrl());
        e.setBio(req.bio());
        e.setOrdre(req.ordre());
        MembreEquipeEntity saved = membreRepo.save(e);
        auditService.log("create", "membre_equipe", saved.getId(), Map.of("nom", saved.getNom()));
        return toMembreDto(saved);
    }

    public MembreEquipeDto updateMembre(UUID id, CreateMembreEquipeRequest req) {
        requireN1();
        MembreEquipeEntity e = membreRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membre introuvable: " + id));
        e.setNom(req.nom());
        e.setPoste(req.poste());
        e.setPhotoUrl(req.photoUrl());
        e.setBio(req.bio());
        e.setOrdre(req.ordre());
        MembreEquipeEntity saved = membreRepo.save(e);
        auditService.log("update", "membre_equipe", saved.getId(), Map.of());
        return toMembreDto(saved);
    }

    public void deleteMembre(UUID id) {
        requireN1();
        membreRepo.deleteById(id);
        auditService.log("delete", "membre_equipe", id, Map.of());
    }

    // ==================== ADMIN — PARTENAIRES ====================

    @Transactional(readOnly = true)
    public List<PartenaireDto> getAllPartenaires() {
        requireN1();
        return partenaireRepo.findAllByOrderByOrdreAsc().stream().map(this::toPartenaireDto).toList();
    }

    public PartenaireDto createPartenaire(CreatePartenaireRequest req) {
        requireN1();
        PartenaireEntity e = new PartenaireEntity();
        e.setId(UUID.randomUUID());
        e.setNom(req.nom());
        e.setLogoUrl(req.logoUrl());
        e.setSiteWeb(req.siteWeb());
        e.setOrdre(req.ordre());
        PartenaireEntity saved = partenaireRepo.save(e);
        auditService.log("create", "partenaire", saved.getId(), Map.of("nom", saved.getNom()));
        return toPartenaireDto(saved);
    }

    public PartenaireDto updatePartenaire(UUID id, CreatePartenaireRequest req) {
        requireN1();
        PartenaireEntity e = partenaireRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Partenaire introuvable: " + id));
        e.setNom(req.nom());
        e.setLogoUrl(req.logoUrl());
        e.setSiteWeb(req.siteWeb());
        e.setOrdre(req.ordre());
        PartenaireEntity saved = partenaireRepo.save(e);
        auditService.log("update", "partenaire", saved.getId(), Map.of());
        return toPartenaireDto(saved);
    }

    public void deletePartenaire(UUID id) {
        requireN1();
        partenaireRepo.deleteById(id);
        auditService.log("delete", "partenaire", id, Map.of());
    }

    // ==================== ADMIN — CANDIDATURES ====================

    @Transactional(readOnly = true)
    public List<CandidaturePubliqueDto> getAllCandidatures() {
        requireN1();
        return candidatureRepo.findAllByOrderByDateSoumissionDesc()
                .stream().map(this::toCandidatureDto).toList();
    }

    public CandidatureAccepteeResult traiterCandidature(UUID id, TraiterCandidatureRequest req) {
        requireN1();
        CandidaturePubliqueEntity cand = candidatureRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidature introuvable: " + id));

        UUID adminId = SecurityUtils.getCurrentUserId();

        if ("refusee".equals(req.statut())) {
            cand.setStatut("refusee");
            cand.setDateTraitement(OffsetDateTime.now());
            cand.setTraitePar(adminId);
            candidatureRepo.save(cand);
            auditService.log("refuse", "candidature_publique", id, Map.of());
            return new CandidatureAccepteeResult(id, null, null, null, null, null);
        }

        if (!"acceptee".equals(req.statut())) {
            throw new IllegalArgumentException("Statut invalide: " + req.statut());
        }

        if (req.communeId() == null) {
            throw new IllegalArgumentException("Une commune doit être spécifiée pour accepter une candidature");
        }

        // 1. Créer l'apprenant
        ApprenantEntity apprenant = new ApprenantEntity();
        apprenant.setId(UUID.randomUUID());
        apprenant.setTerritoireId(req.communeId());
        apprenant.setNom(cand.getNom());
        apprenant.setPrenom(cand.getPrenom());
        apprenant.setTelephone(cand.getTelephone());
        apprenant.setSyncStatus("synced");
        apprenant.setServerUpdatedAt(OffsetDateTime.now());
        jdbcTemplate.update(
                "INSERT INTO apprenant (id, territoire_id, nom, prenom, telephone, sync_status, server_updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                apprenant.getId(), apprenant.getTerritoireId(), apprenant.getNom(),
                apprenant.getPrenom(), apprenant.getTelephone(), "synced", OffsetDateTime.now());

        // 2. Créer l'utilisateur lié (rôle N7_APPRENANT)
        RoleEntity roleN7 = roleRepo.findByCode("N7_APPRENANT")
                .orElseThrow(() -> new IllegalStateException("Rôle N7_APPRENANT introuvable en base"));

        String tempPassword = generateTempPassword();
        String login = cand.getEmail();

        UtilisateurEntity user = new UtilisateurEntity(
                UUID.randomUUID(),
                cand.getNom() + " " + cand.getPrenom(),
                login,
                passwordEncoder.encode(tempPassword),
                roleN7.getId(),
                req.communeId()
        );
        user.setTelephone(cand.getTelephone());
        user.setActif(true);
        user.setSyncStatus("synced");
        user.setServerUpdatedAt(OffsetDateTime.now());
        user.setMetadata(Map.of("must_change_password", true, "apprenant_id", apprenant.getId().toString()));
        utilisateurRepo.save(user);

        // 3. Mettre à jour la candidature
        cand.setStatut("acceptee");
        cand.setDateTraitement(OffsetDateTime.now());
        cand.setCommuneId(req.communeId());
        cand.setTraitePar(adminId);
        candidatureRepo.save(cand);

        auditService.log("accept", "candidature_publique", id,
                Map.of("apprenant_id", apprenant.getId().toString(),
                        "utilisateur_id", user.getId().toString(),
                        "commune_id", req.communeId().toString()));

        // 4. Déterminer le canal de transmission et envoyer les identifiants
        String canal = "afficher_ecran";
        String messageCredentials = String.format(
                "KAMER CINÉ TALENTS — Vos identifiants de connexion: Login: %s, Mot de passe: %s. À changer dès la première connexion.",
                login, tempPassword);

        if (cand.getTelephone() != null && !cand.getTelephone().isBlank()) {
            canal = "sms";
            notificationService.envoyerNotificationSysteme(user.getId(), messageCredentials, true);
        } else if (cand.getEmail() != null && !cand.getEmail().isBlank()) {
            canal = "email";
            notificationService.envoyerNotificationSysteme(user.getId(), messageCredentials, false);
        }

        return new CandidatureAccepteeResult(
                id, apprenant.getId(), user.getId(), login, tempPassword, canal);
    }

    // ==================== ADMIN — MESSAGES DE CONTACT ====================

    @Transactional(readOnly = true)
    public List<ContactMessageDto> getAllMessages() {
        requireN1();
        return contactRepo.findAllByOrderByDateReceptionDesc()
                .stream().map(this::toContactDto).toList();
    }

    public ContactMessageDto marquerMessageTraite(UUID id) {
        requireN1();
        ContactMessageEntity e = contactRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Message introuvable: " + id));
        e.setStatut("traite");
        e.setDateTraitement(OffsetDateTime.now());
        ContactMessageEntity saved = contactRepo.save(e);
        auditService.log("mark_read", "contact_message", saved.getId(), Map.of());
        return toContactDto(saved);
    }

    // ==================== PRIVÉ ====================

    private void requireN1() {
        SecurityContext ctx = SecurityUtils.get();
        if (ctx.niveauHierarchique() != 1 && ctx.niveauHierarchique() != 0) {
            throw new PerimeterAccessException("Accès réservé au Comité Central (niveau 1) ou à l'Administrateur Système (niveau 0)");
        }
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // ==================== MAPPERS ====================

    private ActualitePubliqueDto toActualiteDto(ActualitePubliqueEntity e) {
        return new ActualitePubliqueDto(e.getId(), e.getTitre(), e.getContenu(),
                e.getImageUrl(), e.getDatePublication(), e.getStatut());
    }

    private FaqItemDto toFaqDto(FaqItemEntity e) {
        return new FaqItemDto(e.getId(), e.getQuestion(), e.getReponse(),
                e.getCategorie(), e.getOrdre(), e.isActif());
    }

    private MembreEquipeDto toMembreDto(MembreEquipeEntity e) {
        return new MembreEquipeDto(e.getId(), e.getNom(), e.getPoste(),
                e.getPhotoUrl(), e.getBio(), e.getOrdre());
    }

    private PartenaireDto toPartenaireDto(PartenaireEntity e) {
        return new PartenaireDto(e.getId(), e.getNom(), e.getLogoUrl(),
                e.getSiteWeb(), e.getOrdre());
    }

    private CandidaturePubliqueDto toCandidatureDto(CandidaturePubliqueEntity e) {
        return new CandidaturePubliqueDto(e.getId(), e.getNom(), e.getPrenom(),
                e.getEmail(), e.getTelephone(), e.getMotivation(), e.getStatut(),
                e.getDateSoumission(), e.getDateTraitement(), e.getCommuneId(), e.getTraitePar());
    }

    private ContactMessageDto toContactDto(ContactMessageEntity e) {
        return new ContactMessageDto(e.getId(), e.getNom(), e.getEmail(),
                e.getSujet(), e.getMessage(), e.getStatut(),
                e.getDateReception(), e.getDateTraitement());
    }
}
