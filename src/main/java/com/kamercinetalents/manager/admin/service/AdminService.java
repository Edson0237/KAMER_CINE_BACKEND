package com.kamercinetalents.manager.admin.service;

import com.kamercinetalents.manager.admin.dto.AuditLogDto;
import com.kamercinetalents.manager.admin.dto.FeatureFlagDto;
import com.kamercinetalents.manager.admin.dto.ParametreSystemeDto;
import com.kamercinetalents.manager.admin.repository.AuditLogRepository;
import com.kamercinetalents.manager.admin.repository.FeatureFlagRepository;
import com.kamercinetalents.manager.admin.repository.ParametreSystemeRepository;
import com.kamercinetalents.manager.common.security.SecurityUtils;
import com.kamercinetalents.manager.common.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service d'administration — expose les opérations de consultation du
 * journal d'audit, de gestion des paramètres système et des feature flags.
 *
 * <p>Chaque modification (paramètre, feature flag) est journalisée dans
 * {@code audit_log} via {@link AuditService} dans la même transaction.</p>
 */
@Service
@Transactional(readOnly = true)
public class AdminService {

    private final AuditLogRepository auditLogRepository;
    private final ParametreSystemeRepository parametreSystemeRepository;
    private final FeatureFlagRepository featureFlagRepository;
    private final AuditService auditService;

    /**
     * Construit le service avec ses dépendances injectées.
     *
     * @param auditLogRepository       repository du journal d'audit
     * @param parametreSystemeRepository repository des paramètres système
     * @param featureFlagRepository    repository des feature flags
     * @param auditService             service d'audit transverse
     */
    public AdminService(
            AuditLogRepository auditLogRepository,
            ParametreSystemeRepository parametreSystemeRepository,
            FeatureFlagRepository featureFlagRepository,
            AuditService auditService) {
        this.auditLogRepository = auditLogRepository;
        this.parametreSystemeRepository = parametreSystemeRepository;
        this.featureFlagRepository = featureFlagRepository;
        this.auditService = auditService;
    }

    /**
     * Consulte le journal d'audit avec filtres optionnels.
     *
     * @param utilisateurId filtre par utilisateur (null = tous)
     * @param entiteType     filtre par type d'entité (null = tous)
     * @param pageable       pagination
     * @return une page d'entrées d'audit
     */
    public Page<AuditLogDto> getAuditLog(UUID utilisateurId, String entiteType, Pageable pageable) {
        requirePermission("audit:read");
        return auditLogRepository.findFiltered(utilisateurId, entiteType, pageable)
                .map(e -> new AuditLogDto(
                        e.getId(), e.getUtilisateurId(), e.getAction(),
                        e.getEntiteType(), e.getEntiteId(), e.getDate(), e.getDetails()));
    }

    /**
     * Liste tous les paramètres système.
     *
     * @return la liste des paramètres
     */
    public List<ParametreSystemeDto> getAllParametres() {
        requirePermission("parametre:read");
        return parametreSystemeRepository.findAll().stream()
                .map(e -> new ParametreSystemeDto(
                        e.getId(), e.getCle(), e.getValeur(), e.getType(),
                        e.getDescription(), e.getModifiableParRoleId()))
                .toList();
    }

    /**
     * Modifie la valeur d'un paramètre système.
     *
     * @param cle     la clé du paramètre
     * @param valeur  la nouvelle valeur
     * @return le paramètre mis à jour
     */
    @Transactional
    public ParametreSystemeDto updateParametre(String cle, String valeur) {
        requirePermission("parametre:write");
        var entity = parametreSystemeRepository.findByCle(cle)
                .orElseThrow(() -> new IllegalArgumentException("Paramètre introuvable: " + cle));
        String oldValue = entity.getValeur();
        entity.setValeur(valeur);
        parametreSystemeRepository.save(entity);

        auditService.log("update", "parametre_systeme", entity.getId(),
                Map.of("cle", cle, "ancienne_valeur", oldValue, "nouvelle_valeur", valeur));

        return new ParametreSystemeDto(
                entity.getId(), entity.getCle(), entity.getValeur(),
                entity.getType(), entity.getDescription(), entity.getModifiableParRoleId());
    }

    /**
     * Liste tous les feature flags.
     *
     * @return la liste des feature flags
     */
    public List<FeatureFlagDto> getAllFeatureFlags() {
        requirePermission("feature_flag:read");
        return featureFlagRepository.findAll().stream()
                .map(e -> new FeatureFlagDto(
                        e.getId(), e.getCode(), e.getLibelle(),
                        e.isActif(), e.getVersionCible(), e.getTerritoireId()))
                .toList();
    }

    /**
     * Active ou désactive un feature flag.
     *
     * @param code  le code du flag
     * @param actif le nouvel état
     * @return le flag mis à jour
     */
    @Transactional
    public FeatureFlagDto toggleFeatureFlag(String code, boolean actif) {
        requirePermission("feature_flag:write");
        var entity = featureFlagRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Feature flag introuvable: " + code));
        boolean oldActif = entity.isActif();
        entity.setActif(actif);
        featureFlagRepository.save(entity);

        auditService.log("update", "feature_flag", entity.getId(),
                Map.of("code", code, "ancien_actif", oldActif, "nouvel_actif", actif));

        return new FeatureFlagDto(
                entity.getId(), entity.getCode(), entity.getLibelle(),
                entity.isActif(), entity.getVersionCible(), entity.getTerritoireId());
    }

    private void requirePermission(String permission) {
        if (!SecurityUtils.get().permissions().contains(permission)) {
            throw new com.kamercinetalents.manager.common.exception.PerimeterAccessException(
                    "Permission requise: " + permission);
        }
    }
}
