package com.kamercinetalents.manager.territoire.service;

import com.kamercinetalents.manager.common.dto.PageResponseDto;
import com.kamercinetalents.manager.common.service.AuditService;
import com.kamercinetalents.manager.common.service.TerritoireAccessService;
import com.kamercinetalents.manager.formation.repository.ApprenantRepository;
import com.kamercinetalents.manager.formation.repository.EncadreurRepository;
import com.kamercinetalents.manager.formation.repository.SessionFormationRepository;
import com.kamercinetalents.manager.territoire.domain.StatutCommuneEntity;
import com.kamercinetalents.manager.territoire.domain.TerritoireEntity;
import com.kamercinetalents.manager.territoire.domain.TypeTerritoireEntity;
import com.kamercinetalents.manager.territoire.dto.CommuneDto;
import com.kamercinetalents.manager.territoire.dto.CreateTerritoireRequest;
import com.kamercinetalents.manager.territoire.dto.StatutCommuneDto;
import com.kamercinetalents.manager.territoire.dto.TerritoireDto;
import com.kamercinetalents.manager.territoire.dto.TypeTerritoireDto;
import com.kamercinetalents.manager.territoire.repository.StatutCommuneRepository;
import com.kamercinetalents.manager.territoire.repository.TerritoireRepository;
import com.kamercinetalents.manager.territoire.repository.TypeTerritoireRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service de gestion des territoires (module M2).
 *
 * <p>Chaque opération applique le contrôle du périmètre territorial :
 * un utilisateur ne peut créer, consulter ou modifier que les territoires
 * situés dans sa hiérarchie. La création d'un territoire nécessite que
 * le parent soit dans le périmètre de l'utilisateur courant.</p>
 *
 * <p>Toutes les opérations CUD sont journalisées dans audit_log.</p>
 */
@Service
@Transactional
public class TerritoireService {

    private final TerritoireRepository territoireRepository;
    private final TypeTerritoireRepository typeTerritoireRepository;
    private final StatutCommuneRepository statutCommuneRepository;
    private final TerritoireAccessService territoireAccessService;
    private final AuditService auditService;
    private final ApprenantRepository apprenantRepository;
    private final EncadreurRepository encadreurRepository;
    private final SessionFormationRepository sessionFormationRepository;

    /**
     * Construit le service avec ses dépendances injectées.
     *
     * @param territoireRepository      repository des territoires
     * @param typeTerritoireRepository  repository des types de territoire
     * @param statutCommuneRepository   repository des statuts de commune
     * @param territoireAccessService   service de contrôle du périmètre
     * @param auditService              service d'audit transverse
     * @param apprenantRepository       repository des apprenants
     * @param encadreurRepository       repository des encadreurs
     * @param sessionFormationRepository repository des sessions
     */
    public TerritoireService(
            TerritoireRepository territoireRepository,
            TypeTerritoireRepository typeTerritoireRepository,
            StatutCommuneRepository statutCommuneRepository,
            TerritoireAccessService territoireAccessService,
            AuditService auditService,
            ApprenantRepository apprenantRepository,
            EncadreurRepository encadreurRepository,
            SessionFormationRepository sessionFormationRepository) {
        this.territoireRepository = territoireRepository;
        this.typeTerritoireRepository = typeTerritoireRepository;
        this.statutCommuneRepository = statutCommuneRepository;
        this.territoireAccessService = territoireAccessService;
        this.auditService = auditService;
        this.apprenantRepository = apprenantRepository;
        this.encadreurRepository = encadreurRepository;
        this.sessionFormationRepository = sessionFormationRepository;
    }

    /**
     * Liste tous les territoires du périmètre de l'utilisateur courant.
     *
     * @return la liste des territoires visibles
     */
    @Transactional(readOnly = true)
    public List<TerritoireDto> listAll() {
        return territoireRepository.findAll().stream()
                .filter(t -> t.getDeletedAt() == null)
                .filter(t -> territoireAccessService.canAccess(t.getId()))
                .map(this::toDto)
                .toList();
    }

    /**
     * Crée un nouveau territoire.
     *
     * <p>Le territoire parent doit être dans le périmètre de l'utilisateur
     * courant. L'opération est journalisée dans audit_log.</p>
     *
     * @param request les données de création
     * @return le DTO du territoire créé
     */
    public TerritoireDto create(CreateTerritoireRequest request) {
        if (request.parentId() != null) {
            territoireAccessService.requireAccess(request.parentId());
        }

        TerritoireEntity entity = new TerritoireEntity();
        entity.setId(UUID.randomUUID());
        entity.setCode(request.code());
        entity.setNom(request.nom());
        entity.setTypeTerritoireId(request.typeTerritoireId());
        entity.setParentId(request.parentId());
        entity.setStatutCommuneId(request.statutCommuneId());
        entity.setMetadata(request.metadata());
        entity.setSyncStatus("synced");
        entity.setServerUpdatedAt(OffsetDateTime.now());

        TerritoireEntity saved = territoireRepository.save(entity);

        auditService.log("create", "territoire", saved.getId(),
                Map.of("code", saved.getCode(), "nom", saved.getNom(),
                       "type_territoire_id", saved.getTypeTerritoireId().toString()));

        return toDto(saved);
    }

    /**
     * Récupère un territoire par son identifiant.
     *
     * @param id l'UUID du territoire
     * @return le DTO du territoire
     */
    @Transactional(readOnly = true)
    public TerritoireDto getById(UUID id) {
        TerritoireEntity entity = territoireRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Territoire introuvable: " + id));
        territoireAccessService.requireAccess(id);
        return toDto(entity);
    }

    /**
     * Liste les enfants directs d'un territoire parent.
     *
     * @param parentId l'UUID du parent
     * @return la liste des territoires enfants visibles
     */
    @Transactional(readOnly = true)
    public List<TerritoireDto> getChildren(UUID parentId) {
        territoireAccessService.requireAccess(parentId);
        return territoireRepository.findByParentId(parentId).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Liste tous les types de territoire (table de référence).
     *
     * @return la liste des types
     */
    @Transactional(readOnly = true)
    public List<TypeTerritoireDto> getAllTypes() {
        return typeTerritoireRepository.findAll().stream()
                .map(t -> new TypeTerritoireDto(t.getId(), t.getCode(), t.getLibelle(), t.getNiveau()))
                .toList();
    }

    /**
     * Liste tous les statuts de commune (table de référence).
     *
     * @return la liste des statuts
     */
    @Transactional(readOnly = true)
    public List<StatutCommuneDto> getAllStatuts() {
        return statutCommuneRepository.findAll().stream()
                .map(s -> new StatutCommuneDto(s.getId(), s.getCode(), s.getLibelle()))
                .toList();
    }

    /**
     * Effectue une suppression douce (soft delete) d'un territoire.
     *
     * @param id l'UUID du territoire à supprimer
     */
    public void softDelete(UUID id) {
        TerritoireEntity entity = territoireRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Territoire introuvable: " + id));
        territoireAccessService.requireAccess(id);
        entity.setDeletedAt(OffsetDateTime.now());
        entity.setServerUpdatedAt(OffsetDateTime.now());
        territoireRepository.save(entity);

        auditService.log("delete", "territoire", id, Map.of("soft_delete", true));
    }

    /**
     * Liste les communes du périmètre avec compteurs de déploiement.
     *
     * <p>Récupère les territoires de type "commune" dans le périmètre de
     * l'utilisateur, enrichit chaque commune avec son statut et les compteurs
     * (apprenants, encadreurs, sessions).</p>
     *
     * @return la liste des communes du périmètre
     */
    @Transactional(readOnly = true)
    public List<CommuneDto> getCommunes() {
        TypeTerritoireEntity typeCommune = typeTerritoireRepository.findAll().stream()
                .filter(t -> "commune".equals(t.getCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Type de territoire 'commune' introuvable"));

        List<TerritoireEntity> allCommunes = territoireRepository.findByTypeTerritoireId(typeCommune.getId());

        return allCommunes.stream()
                .filter(c -> c.getDeletedAt() == null)
                .filter(c -> territoireAccessService.canAccess(c.getId()))
                .map(c -> {
                    String statutCode = c.getStatutCommuneId() != null
                            ? statutCommuneRepository.findById(c.getStatutCommuneId())
                                    .map(StatutCommuneEntity::getCode)
                                    .orElse("non_demarree")
                            : "non_demarree";
                    return new CommuneDto(
                            c.getId(),
                            c.getNom(),
                            c.getId(),
                            statutCode,
                            apprenantRepository.countByTerritoireId(c.getId()),
                            encadreurRepository.countByTerritoireId(c.getId()),
                            sessionFormationRepository.countByTerritoireId(c.getId())
                    );
                })
                .toList();
    }

    /**
     * Recherche paginée des communes du périmètre, filtrée par nom.
     *
     * <p>Le filtrage par périmètre territorial dépend du contexte de
     * sécurité et s'applique en mémoire (comme {@link #getCommunes()}) ;
     * la pagination est donc appliquée après filtrage sur la liste
     * résultante. Le volume (360 communes maximum) reste compatible
     * avec ce traitement en mémoire.</p>
     *
     * @param nom  filtre optionnel par nom de commune (insensible à la casse)
     * @param page le numéro de page (0-indexé)
     * @param size la taille de page
     * @return la page de communes correspondant au filtre
     */
    @Transactional(readOnly = true)
    public PageResponseDto<CommuneDto> getCommunesPage(String nom, int page, int size) {
        List<CommuneDto> all = getCommunes();
        String needle = (nom == null || nom.isBlank()) ? null : nom.trim().toLowerCase();
        List<CommuneDto> filtered = needle == null
                ? all
                : all.stream().filter(c -> c.nom().toLowerCase().contains(needle)).toList();

        int totalElements = filtered.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<CommuneDto> pageContent = filtered.subList(fromIndex, toIndex);

        return new PageResponseDto<>(pageContent, page, size, totalElements, totalPages);
    }

    /**
     * Récupère le détail d'une commune avec compteurs.
     *
     * @param id l'UUID de la commune
     * @return le DTO de la commune avec compteurs
     */
    @Transactional(readOnly = true)
    public CommuneDto getCommuneById(UUID id) {
        TerritoireEntity entity = territoireRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commune introuvable: " + id));
        territoireAccessService.requireAccess(id);

        String statutCode = entity.getStatutCommuneId() != null
                ? statutCommuneRepository.findById(entity.getStatutCommuneId())
                        .map(StatutCommuneEntity::getCode)
                        .orElse("non_demarree")
                : "non_demarree";

        return new CommuneDto(
                entity.getId(),
                entity.getNom(),
                entity.getId(),
                statutCode,
                apprenantRepository.countByTerritoireId(id),
                encadreurRepository.countByTerritoireId(id),
                sessionFormationRepository.countByTerritoireId(id)
        );
    }

    /**
     * Convertit une entité JPA en DTO immuable.
     */
    private TerritoireDto toDto(TerritoireEntity entity) {
        return new TerritoireDto(
                entity.getId(),
                entity.getCode(),
                entity.getNom(),
                entity.getTypeTerritoireId(),
                entity.getParentId(),
                entity.getStatutCommuneId(),
                entity.getMetadata(),
                entity.getDeletedAt()
        );
    }
}
